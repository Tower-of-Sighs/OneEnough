package cc.sighs.oneenoughitem.event;
import com.google.gson.JsonElement;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.Event;
import java.util.Map;

/** Lets replacement domains compose before Minecraft decodes resource JSON. */
public final class RawJsonReloadEvent extends Event {
    public final String directory;
    public final ResourceManager resourceManager;
    public final Map<Identifier, JsonElement> results;
    public RawJsonReloadEvent(String directory, ResourceManager resourceManager, Map<Identifier, JsonElement> results) {
        this.directory = directory;
        this.resourceManager = resourceManager;
        this.results = results;
    }
}
