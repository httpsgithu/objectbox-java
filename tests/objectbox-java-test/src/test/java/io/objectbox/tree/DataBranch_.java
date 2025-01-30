
package io.objectbox.tree;

import io.objectbox.EntityInfo;
import io.objectbox.annotation.apihint.Internal;
import io.objectbox.internal.CursorFactory;
import io.objectbox.internal.IdGetter;
import io.objectbox.internal.ToOneGetter;
import io.objectbox.relation.RelationInfo;
import io.objectbox.relation.ToOne;
import io.objectbox.tree.DataBranchCursor.Factory;

// THIS CODE IS GENERATED BY ObjectBox, DO NOT EDIT.

/**
 * Properties for entity "DataBranch". Can be used for QueryBuilder and for referencing DB names.
 */
public final class DataBranch_ implements EntityInfo<DataBranch> {

    // Leading underscores for static constants to avoid naming conflicts with property names

    public static final String __ENTITY_NAME = "DataBranch";

    public static final int __ENTITY_ID = 44;

    public static final Class<DataBranch> __ENTITY_CLASS = DataBranch.class;

    public static final String __DB_NAME = "DataBranch";

    public static final CursorFactory<DataBranch> __CURSOR_FACTORY = new Factory();

    @Internal
    static final DataBranchIdGetter __ID_GETTER = new DataBranchIdGetter();

    public final static DataBranch_ __INSTANCE = new DataBranch_();

    public final static io.objectbox.Property<DataBranch> id =
        new io.objectbox.Property<>(__INSTANCE, 0, 1, long.class, "id", true, "id");

    public final static io.objectbox.Property<DataBranch> uid =
        new io.objectbox.Property<>(__INSTANCE, 1, 2, String.class, "uid");

    public final static io.objectbox.Property<DataBranch> parentId =
        new io.objectbox.Property<>(__INSTANCE, 2, 3, long.class, "parentId", true);

    public final static io.objectbox.Property<DataBranch> metaBranchId =
        new io.objectbox.Property<>(__INSTANCE, 3, 4, long.class, "metaBranchId", true);

    @SuppressWarnings("unchecked")
    public final static io.objectbox.Property<DataBranch>[] __ALL_PROPERTIES = new io.objectbox.Property[]{
        id,
        uid,
        parentId,
        metaBranchId
    };

    public final static io.objectbox.Property<DataBranch> __ID_PROPERTY = id;

    @Override
    public String getEntityName() {
        return __ENTITY_NAME;
    }

    @Override
    public int getEntityId() {
        return __ENTITY_ID;
    }

    @Override
    public Class<DataBranch> getEntityClass() {
        return __ENTITY_CLASS;
    }

    @Override
    public String getDbName() {
        return __DB_NAME;
    }

    @Override
    public io.objectbox.Property<DataBranch>[] getAllProperties() {
        return __ALL_PROPERTIES;
    }

    @Override
    public io.objectbox.Property<DataBranch> getIdProperty() {
        return __ID_PROPERTY;
    }

    @Override
    public IdGetter<DataBranch> getIdGetter() {
        return __ID_GETTER;
    }

    @Override
    public CursorFactory<DataBranch> getCursorFactory() {
        return __CURSOR_FACTORY;
    }

    @Internal
    static final class DataBranchIdGetter implements IdGetter<DataBranch> {
        @Override
        public long getId(DataBranch object) {
            return object.id;
        }
    }

    /** To-one relation "parent" to target entity "DataBranch". */
    public static final RelationInfo<DataBranch, DataBranch> parent =
            new RelationInfo<>(DataBranch_.__INSTANCE, DataBranch_.__INSTANCE, parentId, new ToOneGetter<DataBranch, DataBranch>() {
                @Override
                public ToOne<DataBranch> getToOne(DataBranch entity) {
                    return entity.parent;
                }
            });

    /** To-one relation "metaBranch" to target entity "MetaBranch". */
    public static final RelationInfo<DataBranch, MetaBranch> metaBranch =
            new RelationInfo<>(DataBranch_.__INSTANCE, MetaBranch_.__INSTANCE, metaBranchId, new ToOneGetter<DataBranch, MetaBranch>() {
                @Override
                public ToOne<MetaBranch> getToOne(DataBranch entity) {
                    return entity.metaBranch;
                }
            });

}
