package weathersim.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import weathersim.orography.Orography;
import weathersim.orography.api.Coordinate;
import weathersim.util.LookupTable;

public class Grid {
	
	private final Orography OROGRAPHY;
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, GridEntry>> CELLS = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, GridEntry>>();
	public Grid(Orography oro) {
		this.OROGRAPHY = oro;
	}
	
	private boolean lock = false;
	
	private boolean lock_queue = false;
	
	private final ArrayList<Coordinate> queue = new ArrayList<Coordinate>();
	
	//lazy parameter says that that gridentry should not be ticked (this is required at the edge of the simulation radius
	//so that the ticking GridEntry's have somewhere to "get" the air from / "push" the air to)
	private GridEntry initializeGridCell(int x, int y, boolean lazy) {
		
		GridEntry entry = new GridEntry(x, y, this, LookupTable.getDefaultTemperature(), LookupTable.getDefaultDewpoint());
		
		entry.setLazy(lazy);
		
		return entry;
		
		//return for convenience
	}
	
	//convenience method, not ideal for code readability but ehhh I can't be bothered
	public void setLazy(int x, int y, boolean lazy) {
		GridEntry entry = this.getGridCell(x, y, false);
				
		if(entry != null) entry.setLazy(lazy);
	}
	
	private void newGridCellInternal() {
		
		this.lock_queue = true;
		
		for(Coordinate loopEntry : this.queue) {
			
			int x = loopEntry.getX();
			int y = loopEntry.getY();
			
			GridEntry entry = this.getGridCell(x, y, false);
			
			if(entry == null) {
				ConcurrentHashMap<Integer, GridEntry> row = this.CELLS.get(x);
				if(row == null) {
					row = new ConcurrentHashMap<Integer, GridEntry>();
					this.CELLS.put(x, row);
				}
				
				row.put(y, this.initializeGridCell(x, y, false)); //since they are auto-lazyed
			} else {
				entry.setLazy(false);
			}
		}
		
		this.queue.clear();
		
		this.lock_queue = false;
		
	}
	
	//creates new grid cells and wakes up lazy ones!
	public void newGridCell(int x, int y) {
		//wait synchronously
		while(true) {
			if(!this.lock_queue) break;
		}
		
		this.queue.add(new Coordinate(x, y));
	}
	
	//this is thread-safe
	//debug is used by nothing at all
	public GridEntry getGridCell(int x, int y, boolean debug) {
		
		GridEntry found = null;
		
		ConcurrentHashMap<Integer, GridEntry> lat = CELLS.get(x);
		
		if(lat != null) {
			found = lat.get(y);
		}
		
		return found;
	}
	
	//TODO

	private class GridTickExecutor implements Runnable {
		
		private final Grid grid;
		
		public GridTickExecutor(Grid grid) {
			this.grid = grid;
		}

		@Override
		//this is hideous
		public void run() {
			
			List<GridEntry> toTick = new ArrayList<GridEntry>();
			
			//not synchronized for race condition reasons but for performance reasons (trust me, you want to have this run atomically)
			synchronized(this.grid) {
				//because looping over an entire array isn't resource-intensive, like, at all
				for(ConcurrentHashMap<Integer, GridEntry> row : this.grid.CELLS.values()) {
					for(GridEntry entry : row.values()) {
						GridEntry[] surroundings = this.grid.getAdjacent(entry.getX(), entry.getY());
						float[][] temps = new float[4][];
						float[][] dews = new float[4][];
						float terrainHeight = this.grid.getOrography().getHeightAtPosition(new Coordinate(entry.getX(), entry.getY()));
						
						//ah yes, efficiency
						
						for(int i = 0; i < surroundings.length /* It's 4, but I'll mess this up anyways */; i++) {
							if(surroundings[i] != null) {
								temps[i] = surroundings[i].getTemps();
								dews[i] = surroundings[i].getDews();
							} else {
								temps[i] = null;
								dews[i] = null;
							}
						}
						
						float terrainTemp = this.grid.OROGRAPHY.getHeatAtPosition(new Coordinate(entry.getX(), entry.getY()));
						float terrainDew = this.grid.OROGRAPHY.getDewpointAtPosition(new Coordinate(entry.getX(), entry.getY()));
						
						entry.setSurroundingTemps(temps);
						entry.setSurroundingDews(dews);
						
						entry.setTerrainTemp(terrainTemp);
						entry.setTerrainDew(terrainDew);
						
						entry.setTerrainHeight(terrainHeight);
						
						entry.setTickID(toTick.size());
						
						toTick.add(entry);
					}
				}
			}
			
				GridEntry[] tickedEntries = new GridEntry[toTick.size()];
			
				//hideous parallelization
				toTick.stream().parallel().forEach(entry -> {
					tickedEntries[entry.getTickID()] = entry.tick();
				});
				
				for(GridEntry entry : tickedEntries) {
					
					int x = entry.getX();
					int y = entry.getY();
					
					ConcurrentHashMap<Integer, GridEntry> row = this.grid.CELLS.get(x);
					if(row == null) {
						row = new ConcurrentHashMap<Integer, GridEntry>();
						
						this.grid.CELLS.put(x, row);
					}
					
					row.put(y, entry);
				}
						
			this.grid.newGridCellInternal();
			
			return;
		}
	}
	
	public void tick() {
		if(!this.lock) {
			this.lock = true;
			
				GridTickExecutor executor = new GridTickExecutor(this);
				new Thread(executor).start();
			
			this.lock = false;
		} else {
			//skipping tick
		}
	}
	
	public Orography getOrography() {
		return this.OROGRAPHY;
	}
	
	public GridEntry[] getAdjacent(int x, int y) {
		GridEntry[] found = new GridEntry[4];
		
		found[0] = this.getGridCell(x + 1, y, false);
		found[1] = this.getGridCell(x - 1, y, false);
		found[2] = this.getGridCell(x, y + 1, false);
		found[3] = this.getGridCell(x, y - 1, false);
		
		return found;
	}
}