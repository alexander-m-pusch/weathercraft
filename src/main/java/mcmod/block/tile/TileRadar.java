package mcmod.block.tile;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class TileRadar extends TileEntity implements ITickable {

	private int tickCounter = 0;
	
	public TileRadar(TileEntityType<?> p_i48289_1_) {
		super(p_i48289_1_);
	}

	@Override
	public void tick() {
		
		if(!this.level.isClientSide()) {
			if(tickCounter % 80 != 0) {
				tickCounter++;
				return;
			}
			tickCounter = 0;
			//we're doing server side business
			
			
			
			tickCounter++;
		}
		

	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket(){
	    CompoundNBT nbtTag = new CompoundNBT();
	    //Write your data into the nbtTag
	    return new SUpdateTileEntityPacket(getBlockPos(), -1, nbtTag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
	    CompoundNBT tag = pkt.getTag();
	    //Handle your Data
	    
	    
	}	
	
}
