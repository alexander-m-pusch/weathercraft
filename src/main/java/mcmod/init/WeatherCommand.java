package mcmod.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import mcmod.wxsiminterface.WxsimAdapter;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import weathersim.base.GridEntry;
import weathersim.orography.api.Coordinate;

public class WeatherCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> weatherCommand = Commands.literal("weatherplus").requires((commandSource) -> 
				commandSource.hasPermission(2)).then(Commands.literal("gridentry").then(Commands.literal("stats").executes(WeatherCommand::getEntryStats)))
				.then(Commands.literal("terrain").then(Commands.literal("stats").executes(WeatherCommand::getTerrainStats)));
		
		dispatcher.register(weatherCommand);
	}
	
	private static CommandContext<CommandSource> context;
	
	private static void setContext(CommandContext<CommandSource> parContext) {
		context = parContext;
	}
	
	//convenience method
	private static void sendMessage(StringTextComponent component) throws CommandSyntaxException {
		if(context != null) {
			context.getSource().getPlayerOrException().sendMessage(component, ChatType.CHAT, context.getSource().getPlayerOrException().getUUID());
		}
	}
	
	private static int getTerrainStats(CommandContext<CommandSource> context) throws CommandSyntaxException {
		
		try {
			setContext(context);
			BlockPos pos = context.getSource().getEntity().blockPosition();
			Coordinate coord = WxsimAdapter.getAdapter().convert(pos);
			
			sendMessage(new StringTextComponent("[WeatherPlus] Terrain at " + pos.getX() + ", " + pos.getZ() + "; grid coordinates: " + coord.getX() + ", " + coord.getY() + ": "));
			sendMessage(new StringTextComponent("[WeatherPlus] Terrain height: " + WxsimAdapter.getAdapter().heightCallback(coord)));
			sendMessage(new StringTextComponent("[WeatherPlus] Temperature: " + WxsimAdapter.getAdapter().tempCallback(coord)));
			sendMessage(new StringTextComponent("[WeatherPlus] Dewpoint: " + WxsimAdapter.getAdapter().dewpointCallback(coord)));
			
		} catch (Exception e) {
			
		} finally {
			setContext(null);
		}
		

		
		return 1;
	}
	
	private static int getEntryStats(CommandContext<CommandSource> context) throws CommandSyntaxException {
		
		Entity entity = context.getSource().getEntity();
		
		setContext(context);
		
		//if(entity == null) throw new CommandSyntaxException(null, null, "", 0);

		try {
			Coordinate coords = WxsimAdapter.getAdapter().convert(entity.blockPosition());
			
			GridEntry entry = WxsimAdapter.getAdapter().instance().getGridCell(coords.getX(), coords.getY(), true);
			
			System.out.println("fired");
			
			if(entry == null) {
				context.getSource().getPlayerOrException().sendMessage(new StringTextComponent("[WeatherPlus] Error: there is no GridEntry at your position. This should not happen, contact the dev with a console log."), ChatType.CHAT, context.getSource().getPlayerOrException().getUUID());
				return 1;
			}
			
			sendMessage(new StringTextComponent("[WeatherPlus] GridEntry at " + entry.getX() + ", " + entry.getY() + ", is lazy: " + entry.isLazy()));
			
			sendMessage(new StringTextComponent("[WeatherPlus] Temperatures:"));
			
			for(int i = 0; i < entry.getTemps().length; i++) {
				sendMessage(new StringTextComponent("[WeatherPlus] Temperature at level " + i * 1000 + "m : " + entry.getTemp(i)));
			}
			
			sendMessage(new StringTextComponent("[WeatherPlus] Dewpoints:"));
			
			for(int i = 0; i < entry.getDews().length; i++) {
				sendMessage(new StringTextComponent("[WeatherPlus] Dewpoint at level " + i * 1000 + "m : " + entry.getDew(i)));
			}
			
		} catch (Exception e) {
			System.out.println("Error processing command " + context.getInput());
			e.printStackTrace();
			sendMessage(new StringTextComponent("[WeatherPlus] Something went wrong. See console or let the server administrator have a look at it for you."));
			return 1;
		} finally {
			setContext(null);
		}
		
		return 1;
	}
}