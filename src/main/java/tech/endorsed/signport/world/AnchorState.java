package tech.endorsed.signport.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import tech.endorsed.signport.SignPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnchorState extends PersistentState {

    public AnchorState() { }

    public AnchorState(List<Anchor> anchors) {
        this.anchors = anchors;
    }

    public List<Anchor> anchors = new ArrayList<>();

    public List<Anchor> GetAnchors() {
        return anchors;
    }

//    public static AnchorState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup wrapperLookup) {
//        AnchorState anchorState = new AnchorState();
//
//        var anchorsOpt = tag.getCompound("anchors");
//        if (anchorsOpt.isEmpty()) return null;
//
//        var anchors = anchorsOpt.get();
//        anchors.getKeys().forEach(key -> {
//            Anchor anchor = new Anchor();
//
//            var anchorsCompoundOpt = anchors.getCompound(key);
//            if (anchorsCompoundOpt.isPresent()) {
//                var anchorsCompound = anchorsCompoundOpt.get();
//                anchor.name = key;
//                anchor.pos = BlockPos.ofFloored(
//                        anchorsCompound.getDouble("xPos").orElse(0d),
//                        anchorsCompound.getDouble("yPos").orElse(0d),
//                        anchorsCompound.getDouble("zPos").orElse(0d));
//            }
//            anchorState.anchors.add(anchor);
//        });
//
//        return anchorState;
//    }

//    @Override
//    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
//        NbtCompound anchorsCompound = new NbtCompound();
//
//        anchors.forEach(anchor -> {
//            NbtCompound anchorCompound = new NbtCompound();
//
//            anchorCompound.putInt("xPos", anchor.pos.getX());
//            anchorCompound.putInt("yPos", anchor.pos.getY());
//            anchorCompound.putInt("zPos", anchor.pos.getZ());
//
//            anchorsCompound.put(anchor.name, anchorCompound);
//        });
//
//        nbt.put("anchors", anchorsCompound);
//
//        return nbt;
//    }


    private static final Codec<BlockPos> POS_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("xPos").forGetter(BlockPos::getX),
            Codec.INT.fieldOf("yPos").forGetter(BlockPos::getY),
            Codec.INT.fieldOf("zPos").forGetter(BlockPos::getZ)).apply(inst, BlockPos::new));

    private static final Codec<AnchorState> CODEC = Codec.unboundedMap(Codec.STRING, POS_CODEC).xmap(
            l -> {
        var anchors = new ArrayList<Anchor>();
        for (var entry : l.entrySet()) {
            anchors.add(new Anchor(entry.getKey(), entry.getValue()));
        }
        return new AnchorState(anchors);
    },
            as -> {
        var entries = new HashMap<String, BlockPos>();
        for (var anchor : as.anchors) {
            entries.put(anchor.name, anchor.pos);
        }
        return entries;
    }).fieldOf("anchors").codec();
    
    private static final PersistentStateType<AnchorState> type = new PersistentStateType<>(SignPort.MOD_ID, AnchorState::new, CODEC, DataFixTypes.CHUNK);

    public static AnchorState getServerState(ServerWorld world) {
        if (world == null) return null;
        PersistentStateManager persistentStateManager = world.getPersistentStateManager();

        return persistentStateManager.getOrCreate(type);
    }
}
