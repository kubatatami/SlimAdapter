package net.idik.lib.slimadapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import net.idik.lib.slimadapter.viewinjector.IViewInjector;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linshuaibin on 10/02/2017.
 */

public class SlimAdapter extends AbstractSlimAdapter {

    private SlimAdapter() {
    }

    private List<?> data;

    public SlimAdapter setData(Object[] data) {
        return setData(Arrays.asList(data));
    }

    public SlimAdapter setData(List<?> data) {
        this.data = data;
        return this;
    }

    public List<?> getData() {
        return data;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    private List<Type> dataTypes = new ArrayList<>();

    private Map<Type, IViewHolderCreator> creators = new HashMap<>();

    private IViewHolderCreator defaultCreator = null;

    @Override
    public final synchronized SlimViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Type dataType = dataTypes.get(viewType);
        IViewHolderCreator creator = creators.get(dataType);
        if (creator == null) {
            for (Type t : creators.keySet()) {
                if (((Class) t).isAssignableFrom((Class) dataType)) {
                    creator = creators.get(t);
                    break;
                }
            }
        }
        if (creator == null) {
            if (defaultCreator == null) {
                throw new IllegalArgumentException("Neither the TYPE not The DEFAULT injector found...");
            }
            creator = defaultCreator;
        }
        return creator.create(parent);
    }

    public static SlimAdapter create() {
        return new SlimAdapter();
    }

    public SlimAdapter registerDefault(final int layoutRes, final SlimInjector slimInjector) {
        defaultCreator = new IViewHolderCreator() {
            @Override
            public SlimTypeViewHolder create(ViewGroup parent) {
                return new SlimTypeViewHolder(parent, layoutRes) {
                    @Override
                    protected void onBind(Object data, IViewInjector injector) {
                        slimInjector.onInject(data, injector);

                    }
                };
            }
        };
        return this;
    }

    public <T> SlimAdapter register(final int layoutRes, Class<T> type, final SlimInjector<T> slimInjector) {
        creators.put(type, new IViewHolderCreator<T>() {
            @Override
            public SlimTypeViewHolder<T> create(ViewGroup parent) {
                return new SlimTypeViewHolder<T>(parent, layoutRes) {
                    @Override
                    protected void onBind(T data, IViewInjector injector) {
                        slimInjector.onInject(data, injector);
                    }
                };
            }
        });
        return this;
    }

    public SlimAdapter attachTo(RecyclerView recyclerView) {
        recyclerView.setAdapter(this);
        return this;
    }

    @Override
    public final synchronized int getItemViewType(int position) {
        Object item = getItem(position);
        int index = dataTypes.indexOf(item.getClass());
        if (index == -1) {
            dataTypes.add(item.getClass());
        }
        index = dataTypes.indexOf(item.getClass());
        return index;
    }

    private interface IViewHolderCreator<T> {
        SlimAdapter.SlimTypeViewHolder<T> create(ViewGroup parent);
    }

    private static abstract class SlimTypeViewHolder<T> extends SlimViewHolder<T> {

        SlimTypeViewHolder(ViewGroup parent, int itemLayoutRes) {
            super(parent, itemLayoutRes);
        }

    }

}
