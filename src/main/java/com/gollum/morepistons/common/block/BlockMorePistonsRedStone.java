package com.gollum.morepistons.common.block;

import static com.gollum.morepistons.ModMorePistons.log;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.gollum.morepistons.common.tileentities.TileEntityMorePistonsMoving;
import com.gollum.morepistons.common.tileentities.TileEntityMorePistonsPiston;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMorePistonsRedStone extends BlockMorePistonsBase {
	
	private Icon[] sidesIcon = new Icon[8];
	
	/**
	 * Constructeur
	 * @param id
	 * @param flag
	 * @param texturePrefixe
	 */
	public BlockMorePistonsRedStone(int id, String registerName, boolean isSticky) {
		super(id, registerName, isSticky);
	}
	
	//////////////////////////
	// Gestion des textures //
	//////////////////////////
	
	@Override
	protected void registerIconsSide  (IconRegister iconRegister) {
		for (int i = 0; i < this.sidesIcon.length; i++) {
			this.sidesIcon[i]  = helper.loadTexture(iconRegister, suffixSide+"_"+(i+1));
		}
		this.blockIcon = this.sidesIcon[0];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side) {
		
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityMorePistonsMoving) {
			te = ((TileEntityMorePistonsMoving) te).subTe;
		}
		if (te instanceof TileEntityMorePistonsPiston) {
			this.blockIcon = this.sidesIcon[(((TileEntityMorePistonsPiston)te).multiplier+7) % 8];
		}
		
		Icon icon = super.getBlockTexture(world, x, y, z, side);
		
		this.blockIcon = this.sidesIcon[0];
		
		return icon;
	}
	
	///////////////////////////////////
	// Gestion du signal de redstone //
	///////////////////////////////////
	
	/**
	 * @param int metadata
	 * @return int multiplier
	 */
	public int getMutiplier (World world, int x, int y, int z) {
		
		int multiplier = 1;
		
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityMorePistonsMoving) {
			te = ((TileEntityMorePistonsMoving) te).subTe;
		}
		if (te instanceof TileEntityMorePistonsPiston) {
			multiplier = ((TileEntityMorePistonsPiston)te).multiplier;
		}
		
		return multiplier;
	}
	
	public void applyMutiplier (World world, int x, int y, int z, int multi) {
		
		log.debug ("applyMultiplier = "+multi, "remote="+world.isRemote);
		
		int metadata = world.getBlockMetadata(x, y, z);
		
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityMorePistonsMoving) {
			te = ((TileEntityMorePistonsMoving) te).subTe;
		}
		if (te instanceof TileEntityMorePistonsPiston) {
			((TileEntityMorePistonsPiston)te).multiplier = multi;
		}
		
		world.notifyBlockOfNeighborChange(x, y, z, this.blockID);
	}

	////////////////////////
	// Gestion des events //
	////////////////////////
	
	/**
	 * Affecte la taille du piston
	 * @param length
	 * @return
	 */
	@Override
	public int getLengthInWorld(World world, int x, int y, int z, int orientation) {
		int power = 0;
		int multi = this.getMutiplier(world, x, y, z);
		
		if (this.isIndirectlyPowered(world, x, y, z, orientation)) {
			
			power = world.getBlockPowerInput(x, y, z);
			
			log.debug("getLengthInWorld: power="+power);
			
			power = (power <= 0) ? 16 : power;
			power = (power > 16) ? 16 : power;
			
			log.debug("getLengthInWorld: power="+power);
		}
		
		return power*multi;
	}
	
	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int faceClicked, float par7, float par8, float par9)  {
		int metadata = world.getBlockMetadata(x, y, z);
		int orientation = BlockPistonBase.getOrientation(metadata);
		if (faceClicked == orientation) {
			return false;
		}
		
		int multi = this.getMutiplier(world, x, y, z) % 8 + 1;
		this.applyMutiplier(world, x, y, z, multi);
		world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "random.click", 0.3F, 0.6F);
		
		return true;
	}
	
}
