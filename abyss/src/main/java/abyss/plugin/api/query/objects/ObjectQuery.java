package abyss.plugin.api.query.objects;

import abyss.plugin.api.Area3di;
import abyss.plugin.api.entities.SceneObject;
import abyss.plugin.api.query.EntityQuery;
import abyss.plugin.api.query.results.EntityResultSet;
import abyss.plugin.api.world.WorldTile;

import java.util.List;
import java.util.regex.Pattern;

public final class ObjectQuery implements EntityQuery<ObjectQuery> {

    private int[] ids;

    private String[] names;

    private String[] options;

    private WorldTile tile;

    private Area3di area;

    private Pattern namePattern;

    public ObjectQuery() {
        this.ids = null;
        this.names = null;
        this.options = null;
        this.namePattern = null;
        this.area = null;
        this.tile = null;
    }

    @Override
    public ObjectQuery id(int... ids) {
        this.ids = ids;
        return this;
    }

    @Override
    public ObjectQuery names(String... names) {
        this.names = names;
        return this;
    }

    @Override
    public ObjectQuery names(Pattern pattern) {
        this.namePattern = pattern;
        return this;
    }

    @Override
    public ObjectQuery within(Area3di area) {
        this.area = area;
        return this;
    }

    @Override
    public ObjectQuery tile(WorldTile tile) {
        this.tile = tile;
        return this;
    }

    public ObjectQuery options(String... options) {
        this.options = options;
        return this;
    }

    public EntityResultSet<SceneObject> result() {
        return new EntityResultSet<>(results());
    }

    private native List<SceneObject> results();
}