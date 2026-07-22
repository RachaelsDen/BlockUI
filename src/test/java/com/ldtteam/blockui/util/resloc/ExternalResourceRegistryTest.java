package com.ldtteam.blockui.util.resloc;

import net.minecraft.resources.Identifier;
import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ExternalResourceRegistryTest
{
    @Test
    public void registerGeneratesValidIdentifierAndStoresPath()
    {
        final Path path = Path.of("Some Folder", "My Texture.PNG");
        final Identifier id = ExternalResourceRegistry.register("blockui", path);

        assertEquals(Identifier.fromNamespaceAndPath("blockui", "some_folder/my_texture.png"), id);
        assertEquals(path, ExternalResourceRegistry.getPath(id));
    }

    @Test
    public void isExternalReturnsTrueForRegisteredAndFalseForUnknown()
    {
        final Identifier id = ExternalResourceRegistry.register("blockui", Path.of("a", "b.png"));

        assertTrue(ExternalResourceRegistry.isExternal(id));
        assertFalse(ExternalResourceRegistry.isExternal(Identifier.withDefaultNamespace("stone")));
    }

    @Test
    public void getPathReturnsNullForUnknownIdentifier()
    {
        assertNull(ExternalResourceRegistry.getPath(Identifier.withDefaultNamespace("stone")));
    }

    @Test
    public void registerSameNamespaceDifferentPathsProducesDistinctIdentifiers()
    {
        final Identifier a = ExternalResourceRegistry.register("blockui", Path.of("a.png"));
        final Identifier b = ExternalResourceRegistry.register("blockui", Path.of("b.png"));

        assertNotEquals(a, b);
    }
}
