package net.momirealms.craftengine.proxy.minecraft.network.protocol.game;

import net.momirealms.craftengine.proxy.minecraft.network.protocol.PacketProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.PositionMoveRotationProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.PositionPathProxy;
import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ConstructorInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

@ReflectionProxy(name = "net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket", activeIf = "min_version=1.21.2")
public interface ClientboundEntityPositionSyncPacketProxy extends PacketProxy {
    ClientboundEntityPositionSyncPacketProxy INSTANCE = ASMProxyFactory.create(ClientboundEntityPositionSyncPacketProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket");

    // 1.21.2 - 26.2: (id, PositionMoveRotation values, onGround)
    @ConstructorInvoker(activeIf = "max_version=26.2")
    Object newInstance(int id, @Type(clazz = PositionMoveRotationProxy.class) Object values, boolean onGround);

    // 26.3: (id, PositionPath position, yRot, xRot, onGround) — the rotation left the values record.
    @ConstructorInvoker(activeIf = "min_version=26.3")
    Object newInstance(int id, @Type(clazz = PositionPathProxy.class) Object position, float yRot, float xRot, boolean onGround);

    @FieldGetter(name = "id")
    int getId(Object target);

    @FieldGetter(name = "onGround")
    boolean getOnGround(Object target);

    @FieldGetter(name = "values", activeIf = "max_version=26.2")
    Object getValues(Object target);

    @FieldGetter(name = "position", activeIf = "min_version=26.3")
    Object getPosition(Object target);
}
