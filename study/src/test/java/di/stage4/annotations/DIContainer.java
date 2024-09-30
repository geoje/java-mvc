package di.stage4.annotations;

import di.ConsumerWrapper;
import di.FunctionWrapper;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 스프링의 BeanFactory, ApplicationContext에 해당되는 클래스
 */
class DIContainer {

    private final Set<Object> beans;

    public DIContainer(final Set<Class<?>> classes) {
        this.beans = createBeans(classes);
        beans.forEach(ConsumerWrapper.accept(this::injectBeansToFields));
    }

    public static DIContainer createContainerForPackage(final String rootPackageName) {
        Set<Class<?>> classes = ClassPathScanner.getAllClassesInPackage(rootPackageName);
        return new DIContainer(classes);
    }

    private Set<Object> createBeans(Set<Class<?>> classes) {
        return classes.stream()
                .map(FunctionWrapper.apply(Class::getDeclaredConstructor))
                .peek(constructor -> constructor.setAccessible(true))
                .map(FunctionWrapper.apply(Constructor::newInstance))
                .collect(Collectors.toUnmodifiableSet());
    }

    private void injectBeansToFields(Object bean) {
        for (Field field : bean.getClass().getDeclaredFields()) {
            injectBeansToField(bean, field);
        }
    }

    private void injectBeansToField(Object bean, Field field) {
        if (!field.isAnnotationPresent(Inject.class)) {
            return;
        }

        field.setAccessible(true);
        Class<?> fieldType = field.getType();
        beans.stream()
                .filter(fieldType::isInstance)
                .findAny()
                .ifPresent(ConsumerWrapper.accept(foundBean -> field.set(bean, foundBean)));
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> aClass) {
        return (T) beans.stream()
                .filter(bean -> bean.getClass().equals(aClass))
                .findAny()
                .orElse(null);
    }
}
