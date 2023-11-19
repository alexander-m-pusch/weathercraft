package mcmod.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import mcmod.wxsiminterface.WxsimAdapter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import weathersim.orography.api.Coordinate;
import weathersim.util.Constants;

public class WeatherCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> weatherCommand = Commands.literal("weatherplus").requires((commandSource) -> 
				commandSource.hasPermission(2)).then(Commands.literal("gridentry").then(Commands.literal("stats").executes(WeatherCommand::getEntryStats)))
				.then(Commands.literal("terrain").then(Commands.literal("stats").executes(WeatherCommand::getTerrainStats)));
		
		dispatcher.register(weatherCommand);
	}
	
	
	private static CommandContext<CommandSourceStack> context;
	
	private static void setContext(CommandContext<CommandSourceStack> parContext) {
		context = parContext;
	}
	
	//convenience method
	private static void sendMessage(String text) throws CommandSyntaxException {
		if(context != null) { 
			context.getSource().getPlayer().sendSystemMessage(Component.literal("[WeatherPlus] " + text), false);
		}
	}
	
	private static int getTerrainStats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {	
		try {
			setContext(context);
			BlockPos pos = context.getSource().getEntity().blockPosition();
			Coordinate coord = WxsimAdapter.getAdapter().convert(pos);
			
			sendMessage("Terrain at " + pos.getX() + ", " + pos.getZ() + "; grid coordinates: " + coord.getX() + ", " + coord.getY() + ": ");
			sendMessage("Terrain height: " + WxsimAdapter.getAdapter().heightCallback(coord));
			sendMessage("Temperature: " + WxsimAdapter.getAdapter().tempCallback(coord));
			sendMessage("Dewpoint: " + WxsimAdapter.getAdapter().dewpointCallback(coord));
			
		} catch (Exception e) {
			
		} finally {
			setContext(null);
		}
		
		return 1;
	}
	
	private static int getEntryStats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		
		Entity entity = context.getSource().getEntity();
		
		setContext(context);
		
		//if(entity == null) throw new CommandSyntaxException(null, null, "", 0);

		try {
			Coordinate coords = WxsimAdapter.getAdapter().convert(entity.blockPosition());
			
			float[] temps = new float[Constants.GRIDSIZE_LAYERS];
			float[] dews = new float[Constants.GRIDSIZE_LAYERS];
			
			for(int i = 0; i < Constants.GRIDSIZE_LAYERS; i++) {
				temps[i] = WxsimAdapter.getAdapter().instance().getTemps(coords.getX(), coords.getY(), i, true);
				dews[i] = WxsimAdapter.getAdapter().instance().getDews(coords.getX(), coords.getY(), i, true);
			}
				
			boolean lazy = WxsimAdapter.getAdapter().instance().getLazy(coords.getX(), coords.getY(), true);
			
			sendMessage("GridEntry at " + coords.getX() + ", " + coords.getY() + ", is active: " + lazy);
			
			sendMessage("Temperatures:");
			
			int i = 0;
			for(float temp : temps) {
				sendMessage("Temperature at level " + i * 1000 + "m : " + temp);
				i++;
			}
			
			sendMessage("Dewpoints:");
			
			i = 0;
			for(float dew : dews) {
				sendMessage("Temperature at level " + i * 1000 + "m : " + dew);
				i++;
			}
			
		} catch (Exception e) {
			System.out.println("Error processing command " + context.getInput());
			e.printStackTrace();
			sendMessage("Something went wrong. See console or let the server administrator have a look at it for you.");
			return 1;
		} finally {
			setContext(null);
		}
		
		return 1;
	}
}