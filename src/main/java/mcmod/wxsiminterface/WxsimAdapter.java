package mcmod.wxsiminterface;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import weathersim.grid.Grid;
import weathersim.orography.Orography;
import weathersim.orography.api.Coordinate;
import weathersim.util.Constants;

public class WxsimAdapter {
	private static WxsimAdapter INSTANCE;
	
	private final Grid grid; //the grid
	private final Level world;
	
	private int radius = 80;
	
	private int tickCounter = 0;
	
	private float temperatureBias = 0.0f; //maybe used later, for seasons. TODO for now
	
	private WxsimAdapter(Level world) {
		this.world = world;
		
		Orography orography = new Orography(this::tempCallback, this::dewpointCallback, this::heightCallback);
		
		this.grid = new Grid(orography);
	}

	public void loadAround(BlockPos pos) {
		int x = (pos.getX() / Constants.GRIDSIZE) - radius;
		int z = (pos.getZ() / Constants.GRIDSIZE) - radius;
		
		for(int i = x; i < x + 2 * radius; i++) {
			for(int j = z; j < z + 2 * radius; j++) {
				this.grid.newGridCell(i, j); //be careful not to use x and z here, they are "static" 
			}
		}
	}
	
	public void setLoadRadius(int radius) {
		this.radius = radius;
	}
	
	public void tick() {
		if(tickCounter <= Constants.TIME_DIFF * 20) {
			tickCounter++;
			return;
		}
		
		if(this.world.isClientSide()) throw new IllegalStateException("Somehow, servertick ticked a client world.");
		
		((ServerLevel) this.world).setWeatherParameters(Integer.MAX_VALUE, 0, false, false);
		
		for(Player player : this.world.players()) {
			if(!player.isSpectator()) this.loadAround(player.blockPosition()); //we don't want a spectator loading in chunks
		}
		
		tickCounter = 0; //run only every 5 secs or so, idk
		this.grid.tick(); //this is a beast of a computation, I'll probably have to make this multithreaded, too
	}
	
	
	public static void initialize(Level world) {
		INSTANCE = new WxsimAdapter(world);
	}
	
	public static WxsimAdapter getAdapter() {
		return INSTANCE;
	}
	
	public Grid instance() {
		return this.grid;
	}
	
	//convenience method
	public BlockPos convert(Coordinate coord) {
		return new BlockPos(coord.getX() * Constants.GRIDSIZE, 64, coord.getY() * Constants.GRIDSIZE);
	}
	
	public Coordinate convert(BlockPos pos) {
		return new Coordinate(pos.getX() / Constants.GRIDSIZE, pos.getZ() / Constants.GRIDSIZE);
	}
	
	private Biome getBiome(Coordinate coord) {
		return this.world.getBiome(this.convert(coord)).get();
	}
	
	public float heightCallback(Coordinate coord) {
		
		BlockPos pos = this.convert(coord);
		
		float height = (float) this.world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos).getY();
		
		//clamp for unloaded chunks
		if(height < 60f) {
			height = 64f;
		}
		
		height = height / ((float) this.world.getHeight() + 32f);
		
		return height;
	}
	
	public float dewpointCallback(Coordinate coord) {
		return WxsimBiomeLookupTable.getDewpointForBiome(this.getBiome(coord), this.temperatureBias);
	}
	
	public synchronized float tempCallback(Coordinate coord) {
		return WxsimBiomeLookupTable.getTemperatureForBiome(this.getBiome(coord), this.temperatureBias);
	}
}
