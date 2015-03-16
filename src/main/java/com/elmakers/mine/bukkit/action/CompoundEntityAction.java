package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class CompoundEntityAction extends CompoundAction
{
    protected boolean targetSelf;
    private List<WeakReference<Entity>> entities = new ArrayList<WeakReference<Entity>>();
    private int currentEntity = 0;
    private CastContext actionContext;

    public abstract void prepareEntities(CastContext context, ConfigurationSection parameters, List<WeakReference<Entity>> entities);

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        targetSelf = parameters.getBoolean("target_self", false);
        entities.clear();
        prepareEntities(context, parameters, entities);
    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        actionContext = createContext(context);
        currentEntity = 0;
    }

	@Override
    public SpellResult perform(CastContext context)
	{
        SpellResult result = SpellResult.NO_TARGET;
        while (currentEntity < entities.size())
        {
            Entity entity = entities.get(currentEntity).get();
            if (entity == null)
            {
                skippedActions(context);
                continue;
            }
            actionContext.setTargetEntity(entity);
            actionContext.setTargetLocation(entity.getLocation());
            SpellResult entityResult = performActions(actionContext);
            result = result.min(entityResult);
            if (entityResult == SpellResult.PENDING) {
                break;
            }
            currentEntity++;
            if (currentEntity < entities.size())
            {
                super.reset(context);
            }
        }

		return result;
	}

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("target_self");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("target_self")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        }
    }

    @Override
    public Object clone()
    {
        CompoundEntityAction action = (CompoundEntityAction)super.clone();
        if (action != null) {
            action.entities = new ArrayList<WeakReference<Entity>>(this.entities);
        }
        return action;
    }
}
