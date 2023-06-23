package tech.endorsed.signport.world;

import net.minecraft.block.entity.SignText;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import oshi.util.tuples.Triplet;
import tech.endorsed.signport.SignPort;

public class PortSignEntity {
    public static boolean teleportToDestination(ServerPlayerEntity entity, World world, SignText activeText) {
        // Check if either sides are valid
        Triplet<Boolean, Anchor, World> foundAnchor = isValidPortSignWorld(world, activeText);
        if (!foundAnchor.getA()) {
            foundAnchor = isValidPortSignWorld(world, activeText);
            if (!foundAnchor.getA()) {
                updatePortLink(activeText, false);
                return false;
            }
        }

        if (foundAnchor.getC() == null) return false;
        updatePortLink(activeText, true);

        Anchor anchor = foundAnchor.getB();

        entity.teleport((ServerWorld) foundAnchor.getC(),
                anchor.pos.getX(),
                anchor.pos.getY(),
                anchor.pos.getZ(),
                entity.getYaw(),
                entity.getPitch());

        return true;
    }

    public static Pair<Boolean, Anchor> isValidPortSign(World world, SignText activeText) {
        Triplet<Boolean, Anchor, World> portSign = isValidPortSignWorld(world, activeText);
        return new Pair<>(portSign.getA(), portSign.getB());
    }

    public static Triplet<Boolean, Anchor, World> isValidPortSignWorld(World world, SignText activeText) {
        if  (world == null || world.isClient) return new Triplet<>(false, null, world);

        String line1 = activeText.getMessage(1, false).getString();
        if (!line1.equalsIgnoreCase("[sp]") && !line1.equalsIgnoreCase("[signport]")) return new Triplet<>(false, null, world);
        String line2 = activeText.getMessage(2, false).getString();

        AnchorState state = AnchorState.getServerState((ServerWorld) world);
        if (state == null) return new Triplet<>(false, null, world);

        for (Anchor anchor: state.GetAnchors()) {
            if (line2.equalsIgnoreCase(anchor.name)) {
                return new Triplet<>(true, anchor, world);
            }
        }

        String line3 = activeText.getMessage(3, false).getString();
        ServerWorld dimensionWorld = world.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(line3)));
        assert dimensionWorld != null;

        SignPort.LOGGER.info("Checking for interdimensional teleports");
        AnchorState dimensionalAnchorState = AnchorState.getServerState(dimensionWorld);
        if (dimensionalAnchorState == null) return new Triplet<>(false, null, world);

        for (Anchor anchor: dimensionalAnchorState.GetAnchors()) {
            if (line2.equalsIgnoreCase(anchor.name)) {
                SignPort.LOGGER.info("Found interdimensional teleport");
                return new Triplet<>(true, anchor, dimensionWorld);
            }
        }

        return new Triplet<>(false, null, world);
    }

    public static void updatePortLink(SignText activeText, boolean foundAnchor) {
        MutableText text = (MutableText) activeText.getMessage(1, false);
        if (foundAnchor) {
            text.setStyle(text.getStyle().withColor(0x2FDD48));
        } else {
            text.setStyle(text.getStyle().withColor(0xFF0000));
        }
    }
}
