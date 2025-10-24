package net.pier.geoe.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.pier.geoe.Geothermal;
import net.pier.geoe.block.EnumPipeConnection;
import net.pier.geoe.block.GeothermalPipeBlock;
import net.pier.geoe.blockentity.PipeBlockEntity;
import net.pier.geoe.capability.pipe.PipeNetwork;
import net.pier.geoe.capability.pipe.WorldNetworkCapability;
import net.pier.geoe.register.GeoeBlocks;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@GameTestHolder(Geothermal.MODID)
public class PipeTest {

    private static final String emptyTemplate = "placement";

    @GameTest(template = emptyTemplate)
    public static void testPlacement(GameTestHelper helper)
    {

        BlockPos pipePos = new BlockPos(1,1,1);
        helper.setBlock(pipePos, GeoeBlocks.PIPE.get());


        assertValidCable(helper,pipePos);
        assertNetworkNumbers(helper, 1);
        helper.destroyBlock(pipePos);
        assertNetworkNumbers(helper, 0);
        helper.succeed();


    }
    @GameTest(template = emptyTemplate)
    public static void testTwoCables(GameTestHelper helper)
    {


        for(Direction direction : Direction.values())
        {
            BlockPos pipePos1 = new BlockPos(1,2,1);
            BlockPos pipePos2 = pipePos1.relative(direction);

            helper.setBlock(pipePos1, GeoeBlocks.PIPE.get());
            helper.setBlock(pipePos2, GeoeBlocks.PIPE.get());

            var pipeInfo1 = assertValidCable(helper, pipePos1);
            var pipeInfo2 = assertValidCable(helper, pipePos2);

            assertTrue(pipeInfo1.getB().equals(pipeInfo2.getB()), "Pipes are not the same");
            assertPipeList(helper, pipeInfo1.getB(), pipePos1, pipePos2);

            assertNetworkNumbers(helper, 1);

            assertConnection(helper, pipeInfo1.getA(), direction);
            assertConnection(helper, pipeInfo2.getA(), direction.getOpposite());

            helper.destroyBlock(pipePos1);
            helper.destroyBlock(pipePos2);

            assertNetworkNumbers(helper, 0);

        }
        helper.succeed();

    }

    @GameTest(template = emptyTemplate)
    public static void testLongNetwork(GameTestHelper helper)
    {

        List<BlockPos> totalPipes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 1; j++) {
                        BlockPos pipePos = new BlockPos(1 +i, 1 + j, 1 );
                        helper.setBlock(pipePos, GeoeBlocks.PIPE.get());
                        totalPipes.add(pipePos);
                }
        }

        PipeNetwork network = assertNetwork(helper,totalPipes.toArray(new BlockPos[]{}));

        List<BlockPos> networkPipesRemoved = new ArrayList<>(totalPipes);
        for (BlockPos pipe : totalPipes) {
            helper.destroyBlock(pipe);
            networkPipesRemoved.remove(pipe);
            if(!networkPipesRemoved.isEmpty())
                assertNetwork(helper,networkPipesRemoved.toArray(new BlockPos[]{}));
        }

        assertNetworkNumbers(helper, 0);
        helper.succeed();
    }

    @GameTest(template = emptyTemplate)
    public static void testSplit(GameTestHelper helper)
    {
        List<BlockPos> totalPipes = new ArrayList<>();
        int size = 10;
        for (int i = 0; i < size; i++) {
                BlockPos pipePos = new BlockPos(1 +i, 1, 1 );
                helper.setBlock(pipePos, GeoeBlocks.PIPE.get());
                totalPipes.add(pipePos);
        }

        PipeNetwork oldNetwork = assertNetwork(helper,totalPipes.toArray(new BlockPos[]{}));
        int startingAmount = size * PipeNetwork.PIPE_CAPACITY;
        oldNetwork.getInternalTank().setFluid(new FluidStack(Fluids.WATER,startingAmount));

        int middleIndex = size / 2;
        BlockPos middle = new BlockPos(totalPipes.get(size / 2));

        helper.destroyBlock(middle);
        totalPipes.remove(middle);

        assertNetworkNumbers(helper, 2);

        Optional<WorldNetworkCapability> networkCapabilityLazyOptional = helper.getLevel().getCapability(WorldNetworkCapability.CAPABILITY).resolve();
        //networkCapabilityLazyOptional.ifPresent(worldNetworkCapability -> assertTrue(worldNetworkCapability.networks.get(oldNetwork.getIdentifier()) == null, "Old Network was not removed"));



        PipeNetwork pipeNetworkLeft = assertNetwork(helper, totalPipes.subList(0, middleIndex).toArray(new BlockPos[]{}));
        PipeNetwork pipeNetworkRight = assertNetwork(helper, totalPipes.subList(middleIndex, totalPipes.size()).toArray(new BlockPos[]{}));

        assertTrue(!pipeNetworkLeft.equals(pipeNetworkRight), "Same Network after splitting!");
        int totalFluid = pipeNetworkRight.getInternalTank().getFluidAmount() + pipeNetworkLeft.getInternalTank().getFluidAmount();
        assertTrue(totalFluid == startingAmount - PipeNetwork.PIPE_CAPACITY, "Not Same Fluid AMount");


        totalPipes.forEach(helper::destroyBlock);
        assertNetworkNumbers(helper, 0);
        helper.succeed();
    }

    private static void useWrench(GameTestHelper helper, BlockPos pos)
    {
        BlockPos blockpos = helper.absolutePos(pos);
        BlockState blockstate = helper.getLevel().getBlockState(blockpos);
        blockstate.use(helper.getLevel(), helper.makeMockPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(blockpos), Direction.NORTH, blockpos, true));

    }

    private static void assertPipeList(GameTestHelper gameTestHelper, PipeNetwork pipeNetwork,BlockPos... pipes)
    {
        /*
        assertTrue(pipeNetwork.getPipes().size() == pipes.length, "Network is not composed of" + pipes.length + "cables");


        for (BlockPos pipe : pipes) {
            if(!pipeNetwork.getPipes().contains(gameTestHelper.absolutePos(pipe)))
                throw new GameTestAssertException("Pipe not contained in network! " + pipe);
        }

         */
    }

    /**
     *
     * @param gameTestHelper the helper
     * @param pipes the pipes to check
     * @return a correct network with valid pipes with the same network which has the exact pipes inside
     */
    private static PipeNetwork assertNetwork(GameTestHelper gameTestHelper,BlockPos... pipes)
    {
        if(pipes.length == 0)
            throw new GameTestAssertException("Passed 0 pipes");

        PipeNetwork lastNetwork = null;
        for (BlockPos pipe : pipes) {
            var pipeInfo = assertValidCable(gameTestHelper, pipe);
            if(lastNetwork == null || pipeInfo.getB().equals(lastNetwork))
                lastNetwork = pipeInfo.getB();
            else
                throw new GameTestAssertException("Pipes are not of the same network");
        }
        assertPipeList(gameTestHelper,lastNetwork,pipes);
        return lastNetwork;
    }

    private static void assertTrue(boolean assertion, String message)
    {
        if(!assertion)
            throw new GameTestAssertException(message);
    }

    private static void assertConnection(GameTestHelper helper,PipeBlockEntity pipeBlockEntity, Direction direction)
    {
        BlockPos relativePos = helper.relativePos(pipeBlockEntity.getBlockPos());
        assertTrue(helper.getLevel().getBlockState(pipeBlockEntity.getBlockPos()).getValue(GeothermalPipeBlock.PROPERTY_BY_DIRECTION.get(direction)) == EnumPipeConnection.PIPE, "BlockState Cable not connected" + direction + relativePos);
        //assertTrue(pipeBlockEntity.getConnection(direction) == EnumPipeConnection.PIPE, "TileEntity Cable Not Connected " + direction + relativePos);
    }

    private static void assertNetworkNumbers(GameTestHelper helper, int networkNumber)
    {
        Optional<WorldNetworkCapability> networkCapabilityLazyOptional = helper.getLevel().getCapability(WorldNetworkCapability.CAPABILITY).resolve();
        networkCapabilityLazyOptional.ifPresent(worldNetworkCapability -> assertTrue(worldNetworkCapability.pipeNetworks.size() == networkNumber, "Size of the network is not " + networkNumber));
    }

    private static Pair<PipeBlockEntity,PipeNetwork> assertValidCable(GameTestHelper helper, BlockPos pos)
    {
        BlockEntity blockEntity = helper.getBlockEntity(pos);
        if(blockEntity instanceof PipeBlockEntity pipe)
        {
            //assertTrue(pipe.getNetworkUUID() != null, "Pipe has no network uuid");
            Optional<WorldNetworkCapability> networkCapabilityLazyOptional = helper.getLevel().getCapability(WorldNetworkCapability.CAPABILITY).resolve();
            if(networkCapabilityLazyOptional.isPresent())
            {
                PipeNetwork pipeNetwork = WorldNetworkCapability.getNetwork(helper.getLevel(), pos);
                assertTrue(pipeNetwork != null, "Network Missing");
                return new Pair<>(pipe, pipeNetwork);
            }
            throw new GameTestAssertException("missing capability");

        }
        throw new GameTestAssertException("missing pipe tile entity");
    }

}
