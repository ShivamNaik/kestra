package org.kestra.cli.contexts;

import io.micronaut.context.ApplicationContextConfiguration;
import io.micronaut.context.DefaultApplicationContext;
import io.micronaut.core.io.service.ServiceDefinition;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.inject.BeanDefinitionReference;
import org.kestra.core.plugins.PluginRegistry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Overload the {@link DefaultApplicationContext} in order to add plugins
 * into the {@link io.micronaut.context.DefaultBeanContext}
 */
@SuppressWarnings("rawtypes")
public class KestraApplicationContext extends DefaultApplicationContext {
    private List<BeanDefinitionReference> resolvedBeanReferences;
    private PluginRegistry pluginRegistry;

    public KestraApplicationContext(@Nonnull ApplicationContextConfiguration configuration, PluginRegistry pluginRegistry) {
        super(configuration);
        this.pluginRegistry = pluginRegistry;
    }

    public List<BeanDefinitionReference> resolveBeanDefinitionReferences(ClassLoader classLoader) {
        final SoftServiceLoader<BeanDefinitionReference> definitions = SoftServiceLoader.load(BeanDefinitionReference.class, classLoader);
        List<BeanDefinitionReference> list = new ArrayList<>(300);
        for (ServiceDefinition<BeanDefinitionReference> definition : definitions) {
            if (definition.isPresent()) {
                final BeanDefinitionReference ref = definition.load();
                list.add(ref);
            }
        }

        return list;
    }

    @Override
    protected @Nonnull List<BeanDefinitionReference> resolveBeanDefinitionReferences() {
        if (resolvedBeanReferences != null) {
            return resolvedBeanReferences;
        }

        List<BeanDefinitionReference> result = super.resolveBeanDefinitionReferences();

        if (pluginRegistry != null) {
            pluginRegistry
                .getPlugins()
                .forEach(plugin -> result.addAll(resolveBeanDefinitionReferences(plugin.getClassLoader())));
        }

        resolvedBeanReferences = result;

        return resolvedBeanReferences;
    }
}
