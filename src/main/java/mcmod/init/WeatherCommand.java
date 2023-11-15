package mcmod.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import mcmod.wxsiminterface.WxsimAdapter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.Entity;
import weathersim.base.GridEntry;
import weathersim.orography.api.Coordinate;

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
			
			GridEntry entry = WxsimAdapter.getAdapter().instance().getGridCell(coords.getX(), coords.getY(), true);
			
			System.out.println("fired");
			
			if(entry == null) {
				sendMessage("Error: there is no GridEntry at your position. Either you are in spectator mode or just created this world. If not, please contact a dev with the console log.");
				return 1;
			}
			
			sendMessage("GridEntry at " + entry.getX() + ", " + entry.getY() + ", is lazy: " + entry.isLazy());
			
			sendMessage("Temperatures:");
			
			for(int i = 0; i < entry.getTemps().length; i++) {
				sendMessage("Temperature at level " + i * 1000 + "m : " + entry.getTemp(i));
			}
			
			sendMessage("Dewpoints:");
			
			for(int i = 0; i < entry.getDews().length; i++) {
				sendMessage("Dewpoint at level " + i * 1000 + "m : " + entry.getDew(i));
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