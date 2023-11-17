package mcmod.init;

import mcmod.wxsiminterface.WxsimAdapter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WeatherCraft.MOD_ID)
public class WeatherCraft {
	
	public static final String MOD_ID = "weatherplus";
	
	public WeatherCraft() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void commonSetup(FMLCommonSetupEvent event) {
		System.out.println("Fired!");
	}
	
	@SubscribeEvent
	public void serverStarted(ServerStartingEvent event) {
		WxsimAdapter.initialize(event.getServer().overworld());
	}
	
	@SubscribeEvent
	public void serverTick(TickEvent.ServerTickEvent event) {
		WxsimAdapter.getAdapter().tick();
	}
	
	@SubscribeEvent
	public void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		WxsimAdapter.getAdapter().loadAround(event.getEntity().blockPosition());
	}
	
	@SubscribeEvent
	public void commandRegisterEvent(RegisterCommandsEvent event) {
		WeatherCommand.register(event.getDispatcher());
	}
}
