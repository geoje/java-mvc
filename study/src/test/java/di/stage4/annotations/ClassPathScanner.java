package di.stage4.annotations;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;

public class ClassPathScanner {

    public static Set<Class<?>> getAllClassesInPackage(final String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> services = reflections.getTypesAnnotatedWith(Service.class);
        Set<Class<?>> repositories = reflections.getTypesAnnotatedWith(Repository.class);

        return CombineSets(services, repositories);
    }

    @SafeVarargs
    private static <T> Set<T> CombineSets(Set<T>... sets) {
        return Arrays.stream(sets)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }
}
