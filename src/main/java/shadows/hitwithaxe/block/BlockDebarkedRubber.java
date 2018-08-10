package shadows.hitwithaxe.block;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import shadows.hitwithaxe.EnumBarkType;
import shadows.placebo.Placebo;
import shadows.placebo.util.PlaceboUtil;
import thebetweenlands.client.tab.BLCreativeTabs;
import thebetweenlands.common.registries.BlockRegistry;

public class BlockDebarkedRubber extends BlockDebarkedLog {

	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool EAST = PropertyBool.create("east");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool WEST = PropertyBool.create("west");
	public static final PropertyBool UP = PropertyBool.create("up");
	public static final PropertyBool DOWN = PropertyBool.create("down");

	protected static final AxisAlignedBB[] BOUNDING_BOXES = new AxisAlignedBB[] {
			//CENTER
			new AxisAlignedBB(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D),
			//NORTH
			new AxisAlignedBB(0.25D, 0.25D, 0.0D, 0.75D, 0.75D, 0.25D),
			//SOUTH
			new AxisAlignedBB(0.25D, 0.25D, 0.75D, 0.75D, 0.75D, 1.0D),
			//EAST
			new AxisAlignedBB(0.75D, 0.25D, 0.25D, 1.0D, 0.75D, 0.75D),
			//WEST
			new AxisAlignedBB(0.0D, 0.25D, 0.25D, 0.25D, 0.75D, 0.75D),
			//UP
			new AxisAlignedBB(0.25D, 0.75D, 0.25D, 0.75D, 1.0D, 0.75D),
			//DOWN
			new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.25D, 0.75D), };
	protected static final AxisAlignedBB[] COMBINED_BOUNDING_BOXES = new AxisAlignedBB[64];

	static {
		List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
		for (int i = 0; i < 64; i++) {
			boolean north = (i & 1) == 1;
			boolean south = ((i >> 1) & 1) == 1;
			boolean east = ((i >> 2) & 1) == 1;
			boolean west = ((i >> 3) & 1) == 1;
			boolean up = ((i >> 4) & 1) == 1;
			boolean down = ((i >> 5) & 1) == 1;
			boxes.clear();
			boxes.add(BOUNDING_BOXES[0]);
			if (north) boxes.add(BOUNDING_BOXES[1]);
			if (south) boxes.add(BOUNDING_BOXES[2]);
			if (east) boxes.add(BOUNDING_BOXES[3]);
			if (west) boxes.add(BOUNDING_BOXES[4]);
			if (up) boxes.add(BOUNDING_BOXES[5]);
			if (down) boxes.add(BOUNDING_BOXES[6]);
			double minX = 1.0D;
			double minY = 1.0D;
			double minZ = 1.0D;
			double maxX = 0.0D;
			double maxY = 0.0D;
			double maxZ = 0.0D;
			for (AxisAlignedBB box : boxes) {
				if (box.minX < minX) minX = box.minX;
				if (box.minY < minY) minY = box.minY;
				if (box.minZ < minZ) minZ = box.minZ;
				if (box.maxX > maxX) maxX = box.maxX;
				if (box.maxY > maxY) maxY = box.maxY;
				if (box.maxZ > maxZ) maxZ = box.maxZ;
			}
			COMBINED_BOUNDING_BOXES[i] = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
		}
	}

	public static AxisAlignedBB getCombinedBoundingBoxForState(IBlockState state) {
		int index = 0;
		if (state.getValue(NORTH)) index |= 1;
		if (state.getValue(SOUTH)) index |= 2;
		if (state.getValue(EAST)) index |= 4;
		if (state.getValue(WEST)) index |= 8;
		if (state.getValue(UP)) index |= 16;
		if (state.getValue(DOWN)) index |= 32;
		return COMBINED_BOUNDING_BOXES[index];
	}

	public BlockDebarkedRubber(EnumBarkType e) {
		super(e);
		this.setHardness(2.0F);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
		this.setCreativeTab(BLCreativeTabs.BLOCKS);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, UP, DOWN, NORTH, SOUTH, EAST, WEST);
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		return false;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state.withProperty(NORTH, this.canConnectTo(worldIn, pos.north())).withProperty(EAST, this.canConnectTo(worldIn, pos.east())).withProperty(SOUTH, this.canConnectTo(worldIn, pos.south())).withProperty(WEST, this.canConnectTo(worldIn, pos.west())).withProperty(UP, this.canConnectTo(worldIn, pos.up())).withProperty(DOWN, worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP) || this.canConnectTo(worldIn, pos.down()));
	}

	public boolean canConnectTo(IBlockAccess worldIn, BlockPos pos) {
		Block block = worldIn.getBlockState(pos).getBlock();
		return block == this || block == BlockRegistry.LEAVES_RUBBER_TREE || block == BlockRegistry.LOG_RUBBER;
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		switch (rot) {
		case CLOCKWISE_180:
			return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(EAST, state.getValue(WEST)).withProperty(SOUTH, state.getValue(NORTH)).withProperty(WEST, state.getValue(EAST));
		case COUNTERCLOCKWISE_90:
			return state.withProperty(NORTH, state.getValue(EAST)).withProperty(EAST, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(WEST)).withProperty(WEST, state.getValue(NORTH));
		case CLOCKWISE_90:
			return state.withProperty(NORTH, state.getValue(WEST)).withProperty(EAST, state.getValue(NORTH)).withProperty(SOUTH, state.getValue(EAST)).withProperty(WEST, state.getValue(SOUTH));
		default:
			return state;
		}
	}

	@Override
	@Deprecated
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		switch (mirrorIn) {
		case LEFT_RIGHT:
			return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(NORTH));
		case FRONT_BACK:
			return state.withProperty(EAST, state.getValue(WEST)).withProperty(WEST, state.getValue(EAST));
		default:
			return super.withMirror(state, mirrorIn);
		}
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	@Deprecated
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
		state = state.getActualState(worldIn, pos);

		addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDING_BOXES[0]);

		if (state.getValue(NORTH)) addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDING_BOXES[1]);

		if (state.getValue(SOUTH)) addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDING_BOXES[2]);

		if (state.getValue(EAST)) addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDING_BOXES[3]);

		if (state.getValue(WEST)) addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDING_BOXES[4]);

		if (state.getValue(UP)) addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDING_BOXES[5]);

		if (state.getValue(DOWN)) addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDING_BOXES[6]);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		state = this.getActualState(state, source, pos);
		return getCombinedBoundingBoxForState(state);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	public void initModels(ModelRegistryEvent e) {
		Placebo.PROXY.useRenamedMapper(this, "rubber_log");
		PlaceboUtil.sMRL("rubber_log", this, 0, "inventory");
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getDefaultState();
	}
}
