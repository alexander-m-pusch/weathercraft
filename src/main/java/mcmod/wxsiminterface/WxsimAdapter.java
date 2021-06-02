package mcmod.wxsiminterface;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.server.ServerWorld;
import weathersim.base.Grid;
import weathersim.orography.Orography;
import weathersim.orography.api.Coordinate;

public class WxsimAdapter {
	private static WxsimAdapter INSTANCE;
	
	private final Grid grid; //the grid
	private final World world;
	private final int resolution;
	
	private int radius = 80;
	
	private int tickCounter = 0;
	
	private float temperatureBias = 0.0f; //maybe used later, for seasons. TODO for now
	
	private WxsimAdapter(World world, int blocksPerGridEntry) {
		this.world = world;
		this.resolution = blocksPerGridEntry;
		
		Orography orography = new Orography(this::tempCallback, this::dewpointCallback, this::heightCallback);
		
		this.grid = new Grid(orography);
	}
	
	public float[] getDewsAtPosition(BlockPos pos) {
		Coordinate cPos = this.convert(pos);
		
		int x = cPos.getX();
		int z = cPos.getY();
		
		return this.grid.getGridCell(x, z, false).getTemps();
	}
	
	public float[] getTempsAtPosition(BlockPos pos) {
		Coordinate cPos = this.convert(pos);
		
		int x = cPos.getX();
		int z = cPos.getY();
		
		return this.grid.getGridCell(x, z, false).getDews();
	}
	
	public void loadAround(BlockPos pos) {
		int x = (pos.getX() / resolution) - radius;
		int z = (pos.getZ() / resolution) - radius;
		
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
		if(tickCounter <= 100) {
			tickCounter++;
			return;
		}
		
		if(this.world.isClientSide()) throw new IllegalStateException("Somehow, servertick ticked a client world.");
		
		((ServerWorld) this.world).setWeatherParameters(Integer.MAX_VALUE, 0, false, false);
		
		for(PlayerEntity player : this.world.players()) {
			if(!player.isSpectator()) this.loadAround(player.blockPosition()); //we don't want a spectator loading in chunks
		}
		
		tickCounter = 0; //run only every 5 secs or so, idk
		this.grid.tick(); //this is a beast of a computation, I'll probably have to make this multithreaded, too
	}
	
	
	public static void initialize(World world, int resolution) {
		INSTANCE = new WxsimAdapter(world, resolution);
	}
	
	public static WxsimAdapter getAdapter() {
		return INSTANCE;
	}
	
	public Grid instance() {
		return this.grid;
	}
	
	//convenience method
	public BlockPos convert(Coordinate coord) {
		return new BlockPos(coord.getX() * this.resolution, 64, coord.getY() * this.resolution);
	}
	
	public Coordinate convert(BlockPos pos) {
		return new Coordinate(pos.getX() / this.resolution, pos.getZ() / this.resolution);
	}
	
	private Biome getBiome(Coordinate coord) {
		return this.world.getBiome(this.convert(coord));
	}
	
	public float heightCallback(Coordinate coord) {
		
		BlockPos pos = this.convert(coord);
		
		float height = (float) this.world.getHeightmapPos(Type.WORLD_SURFACE, pos).getY();
		
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
