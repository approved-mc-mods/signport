package tech.endorsed.signport.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import tech.endorsed.signport.SignPort;

import java.util.ArrayList;
import java.util.List;

public class AnchorState extends PersistentState {
    public List<Anchor> anchors = new ArrayList<>();

    public List<Anchor> GetAnchors() {
        return anchors;
    }

    public static AnchorState createFromNbt(NbtCompound tag) {
        AnchorState anchorState = new AnchorState();

        NbtCompound anchors = tag.getCompound("anchors");
        anchors.getKeys().forEach(key -> {
            Anchor anchor = new Anchor();

            NbtCompound anchorsCompound = anchors.getCompound(key);
            anchor.name = key;
            anchor.pos = BlockPos.ofFloored(
                    anchorsCompound.getDouble("xPos"),
                    anchorsCompound.getDouble("yPos"),
                    anchorsCompound.getDouble("zPos"));
            anchorState.anchors.add(anchor);
        });

        return anchorState;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound anchorsCompound = new NbtCompound();

        anchors.forEach(anchor -> {
            NbtCompound anchorCompound = new NbtCompound();

            anchorCompound.putInt("xPos", anchor.pos.getX());
            anchorCompound.putInt("yPos", anchor.pos.getY());
            anchorCompound.putInt("zPos", anchor.pos.getZ());

            anchorsCompound.put(anchor.name, anchorCompound);
        });

        nbt.put("anchors", anchorsCompound);

        return nbt;
    }

    public static AnchorState getServerState(ServerWorld world) {
        if (world == null) return null;
        PersistentStateManager persistentStateManager = world.getPersistentStateManager();

        return persistentStateManager.getOrCreate(
                AnchorState::createFromNbt,
                AnchorState::new,
                SignPort.MOD_ID);
    }
}
