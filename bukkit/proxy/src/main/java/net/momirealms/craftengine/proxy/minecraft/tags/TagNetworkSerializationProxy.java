package net.momirealms.craftengine.proxy.minecraft.tags;

import net.momirealms.craftengine.proxy.minecraft.core.LayeredRegistryAccessProxy;
import net.momirealms.craftengine.proxy.minecraft.network.FriendlyByteBufProxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

import java.util.Map;

@ReflectionProxy(name = "net.minecraft.tags.TagNetworkSerialization")
public interface TagNetworkSerializationProxy {
    TagNetworkSerializationProxy INSTANCE = ASMProxyFactory.create(TagNetworkSerializationProxy.class);

    @MethodInvoker(name = "serializeTagsToNetwork", isStatic = true)
    Map<Object, Object> serializeTagsToNetwork(@Type(clazz = LayeredRegistryAccessProxy.class) Object registryAccess);

    @ReflectionProxy(name = "net.minecraft.tags.TagNetworkSerialization$NetworkPayload")
    interface NetworkPayloadProxy {
        NetworkPayloadProxy INSTANCE = ASMProxyFactory.create(NetworkPayloadProxy.class);

        // Up to 26.2 the payload wrote and read itself; 26.3 turned it into a record whose only
        // wire format is its STREAM_CODEC. TagUtils picks the path for the running server.
        @MethodInvoker(name = "write", activeIf = "max_version=26.2")
        void write(Object target, @Type(clazz = FriendlyByteBufProxy.class) Object buf);

        @MethodInvoker(name = "read", isStatic = true, activeIf = "max_version=26.2")
        Object read(@Type(clazz = FriendlyByteBufProxy.class) Object buf);

        @FieldGetter(name = "STREAM_CODEC", isStatic = true, activeIf = "min_version=26.3")
        Object getStreamCodec();
    }
}
