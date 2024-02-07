/*
 * Copyright 2022-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.instancio;

import org.instancio.documentation.ExperimentalApi;
import org.instancio.exception.InstancioApiException;
import org.instancio.internal.ApiValidator;
import org.instancio.internal.selectors.FieldSelectorBuilderImpl;
import org.instancio.internal.selectors.PredicateScopeImpl;
import org.instancio.internal.selectors.PredicateSelectorImpl;
import org.instancio.internal.selectors.PrimitiveAndWrapperSelectorImpl;
import org.instancio.internal.selectors.ScopeImpl;
import org.instancio.internal.selectors.SelectorGroupImpl;
import org.instancio.internal.selectors.SelectorImpl;
import org.instancio.internal.selectors.TargetClass;
import org.instancio.internal.selectors.TargetFieldName;
import org.instancio.internal.selectors.TargetGetterReference;
import org.instancio.internal.selectors.TargetSetterName;
import org.instancio.internal.selectors.TargetSetterReference;
import org.instancio.internal.selectors.TypeSelectorBuilderImpl;
import org.instancio.settings.Keys;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * Provides static factory methods for creating selectors and selector scopes.
 * Selectors are used for targeting fields and classes.
 * Instancio supports two types of selectors: regular and predicate-based.
 *
 * <p>Regular selectors allow matching by exact class (not including subtypes)
 * and field:</p>
 *
 * <ul>
 *   <li>{@link #all(Class)}</li>
 *   <li>{@link #field(GetMethodSelector)}</li>
 *   <li>{@link #field(Class, String)}</li>
 *   <li>{@link #field(String)}</li>
 * </ul>
 *
 * <p>Predicate selectors, as the name suggests, use a {@link Predicate}
 * for matching targets:
 *
 * <ul>
 *   <li>{@link #fields()}</li>
 *   <li>{@link #types()}</li>
 *   <li>{@link #fields(Predicate)}</li>
 *   <li>{@link #types(Predicate)}</li>
 * </ul>
 *
 * <p>The first two allow constructing predicates using convenience builder
 * methods. The last two methods can be used with arbitrary predicates
 * where the builders are not sufficient.</p>
 *
 * <p>It should be noted that regular selectors have higher precedence than
 * predicate selectors. See the
 * <a href="https://www.instancio.org/user-guide/#selectors">Selectors</a>
 * section of the User Guide for more details.
 *
 * @see Selector
 * @see SelectorGroup
 * @see PredicateSelector
 * @see TargetSelector
 * @see GroupableSelector
 * @since 1.2.0
 */
public final class Select {

    /**
     * Provides a builder for selecting fields based on {@link Predicate Predicates}.
     * This method can be used for selecting multiple fields in different classes.
     * The returned builder offers convenience methods for constructing the predicate.
     *
     * <p>The following example will match all fields named {@code lastModified}
     * declared in the {@code Person} and other classes referenced from {@code Person}.</p>
     *
     * <pre>{@code
     * Person person = Instancio.of(Person.class)
     *     .supply(fields().named("lastModified"), () -> LocalDateTime.now())
     *     .create();
     * }</pre>
     *
     * <p>Specifying only {@code fields()} (without further predicates)
     * will match all fields declared across the class tree.</p>
     *
     * <p>The alternative method {@link #fields(Predicate)} can be used to specify
     * a predicate directly.</p>
     *
     * @return predicate selector builder for matching fields
     * @see #fields(Predicate)
     * @see #types(Predicate)
     * @see #types()
     * @since 1.6.0
     */
    public static FieldSelectorBuilder fields() {
        return new FieldSelectorBuilderImpl();
    }

    /**
     * Provides a builder for selecting types based on {@link Predicate Predicates}.
     * This method can be used for selecting multiple types.
     * The returned builder offers convenience methods for constructing the predicate.
     *
     * <p>The following example will match all types annotated {@code @Embeddable}.</p>
     *
     * <pre>{@code
     * Person person = Instancio.of(Person.class)
     *     .set(types().annotated(Embeddable.class), null)
     *     .create();
     * }</pre>
     *
     * <p>Specifying only {@code types()} (without further predicates)
     * will match all types referenced in the class tree.</p>
     *
     * <p>The alternative method {@link #types(Predicate)} can be used to specify
     * a predicate directly.</p>
     *
     * @return predicate selector builder for matching types
     * @see #types(Predicate)
     * @see #fields(Predicate)
     * @see #fields()
     * @since 1.6.0
     */
    public static TypeSelectorBuilder types() {
        return new TypeSelectorBuilderImpl();
    }

    /**
     * Select all fields matching the specified predicate.
     *
     * @param predicate for matching fields
     * @return a predicate selector
     * @see #fields()
     * @see #types()
     * @see #types(Predicate)
     * @since 1.6.0
     */
    public static PredicateSelector fields(final Predicate<Field> predicate) {
        ApiValidator.notNull(predicate, "Field predicate must not be null");
        return PredicateSelectorImpl.builder().fieldPredicate(predicate).build();
    }

    /**
     * Select all types matching the specified predicate.
     *
     * @param predicate for matching types
     * @return a predicate selector
     * @see #types()
     * @see #fields()
     * @see #fields(Predicate)
     * @since 1.6.0
     */
    public static PredicateSelector types(final Predicate<Class<?>> predicate) {
        ApiValidator.notNull(predicate, "Type predicate must not be null");
        return PredicateSelectorImpl.builder().typePredicate(predicate).build();
    }

    /**
     * Select all instances of the given type, <b>not including</b> subtypes.
     * <p>
     * If the type is a primitive or wrapper, this method only selects the specified type. For example:
     * <ul>
     *   <li>{@code all(int.class)} - selects primitive {@code int} but not {@code Integer}</li>
     *   <li>{@code all(Integer.class)} - selects {@code Integer} wrapper but not primitive {@code int}</li>
     * </ul>
     * <p>
     * In order to select both, primitive {@code int} and wrapper, use the {@link #allInts()}.
     *
     * @param type to select
     * @return a selector for given class
     * @since 1.2.0
     */
    public static Selector all(final Class<?> type) {
        ApiValidator.notNull(type, "Class must not be null");
        return SelectorImpl.builder()
                .target(new TargetClass(type))
                .build();
    }

    /**
     * A convenience method for combining multiple selectors.
     *
     * <p>Example:
     * <pre>{@code
     * Person person = Instancio.of(Person.class)
     *     .withNullable(all(
     *         all(Gender.class),
     *         all(Phone.class),
     *         field(Person::getDateOfBirth)
     *     ))
     *     .create()
     * }</pre>
     *
     * @param selectors to combine
     * @return a group containing given selectors
     * @since 1.3.0
     */
    public static SelectorGroup all(final GroupableSelector... selectors) {
        ApiValidator.notEmpty(selectors, "Selector group must contain at least one selector");
        return new SelectorGroupImpl(selectors);
    }

    /**
     * Selects a field by name in the specified class. The name must match exactly.
     *
     * @param declaringClass class declaring the field
     * @param fieldName      field name to select
     * @return a selector for given field
     * @throws InstancioApiException if the class has no field with the specified name
     * @see #field(String)
     * @see #fields()
     * @see #fields(Predicate)
     * @since 1.2.0
     */
    public static Selector field(final Class<?> declaringClass, final String fieldName) {
        ApiValidator.notNull(declaringClass, "declaring class must not be null");
        ApiValidator.notNull(fieldName, "field name must not be null");

        return SelectorImpl.builder()
                .target(new TargetFieldName(declaringClass, fieldName))
                .build();
    }

    /**
     * Selects a field by name declared in the class being created.
     * <p>
     * Example
     * <pre>{@code
     * Person person = Instancio.of(Person.class)
     *     .ignore(field("fullName")) // Person.fullName
     *     .create();
     * }</pre>
     * <p>
     *
     * @param fieldName field name to select
     * @return a selector for given field
     * @throws InstancioApiException if the class being created has no field with the specified name
     * @see #field(Class, String)
     * @see #fields()
     * @see #fields(Predicate)
     * @since 1.2.0
     */
    public static Selector field(final String fieldName) {
        ApiValidator.notNull(fieldName, "field name must not be null");
        return SelectorImpl.builder()
                .target(new TargetFieldName(null, fieldName))
                .build();
    }

    /**
     * Selects a field based on the given method reference.
     * <p>
     * Internally, the method reference is mapped to a regular field selector
     * as returned by {@link #field(Class, String)}. Therefore, this
     * method only works for classes with expected field and method naming
     * conventions:
     *
     * <ul>
     *   <li>Java beans convention with "get" and "is" prefixes.</li>
     *   <li>Java records convention where method names match property names.</li>
     * </ul>
     * <p>
     * <b>Examples:</b>
     *
     * <pre>{@code
     * // Java beans naming convention
     * field(Person::getName)  -> field(Person.class, "name")
     * field(Person::isActive) -> field(Person.class, "active")
     *
     * // Property-style naming convention (e.g. Java records)
     * field(Person::name)     -> field(Person.class, "name")
     * field(Person::isActive) -> field(Person.class, "isActive")
     * }</pre>
     * <p>
     * <b>Note:</b> for a method reference with a generic return type,
     * the type must be specified explicitly, for example:
     * <p>
     *
     * <pre>{@code
     * class Item<T> {
     *     private T value;
     *
     *     T getValue() { // generic return type
     *         return value;
     *     }
     * }
     *
     * // Option 1:
     * Select.field(Item<String>::getValue)
     *
     * // Option 2:
     * GetMethodSelector<Item<String>, String> getterSelector = Item::getValue;
     * Select.field(getterSelector)
     * }</pre>
     *
     * @param methodReference method reference from which field name will be resolved
     * @param <T>             type declaring the method
     * @param <R>             return type of the method
     * @return a field selector matching the given method reference
     * @see #field(Class, String)
     * @since 2.3.0
     */
    public static <T, R> Selector field(final GetMethodSelector<T, R> methodReference) {
        ApiValidator.notNull(methodReference, "getter method reference must not be null");
        return SelectorImpl.builder()
                .target(new TargetGetterReference(methodReference))
                .build();
    }

    /**
     * Selects a setter by name declared in the class being created.
     * The setter method must have exactly one parameter. Since this method
     * resolves the setter by name only (ignoring the parameter type)
     * it should only be used if there are no overloaded setters.
     *
     * <p>Example
     * <pre>{@code
     * Person person = Instancio.of(Person.class)
     *     .ignore(setter("setFullName")) // Person.setFullName
     *     .create();
     * }</pre>
     *
     * <p><b>Note:</b> setter selectors can only be used if
     * {@link Keys#ASSIGNMENT_TYPE} is set to {@code AssignmentType.METHOD}.
     *
     * @param methodName the name of the setter method to select
     * @return a selector for given method
     * @throws InstancioApiException if the root class has no method with the specified name
     * @see #setter(Class, String)
     * @see #setter(Class, String, Class)
     * @since 4.0.0
     */
    @ExperimentalApi
    public static Selector setter(final String methodName) {
        return setter(null, methodName, null);
    }

    /**
     * Selects a setter method by name in the specified class.
     * The method must have exactly one parameter. Since this method resolves
     * the setter by name only (ignoring the parameter type)
     * it should only be used if there are no overloaded setters.
     *
     * <p><b>Note:</b> setter selectors can only be used if
     * {@link Keys#ASSIGNMENT_TYPE} is set to {@code AssignmentType.METHOD}.
     *
     * @param declaringClass class declaring the method
     * @param methodName     method name to select
     * @return a selector for given method
     * @throws InstancioApiException if the class has no method with the specified name
     * @see #setter(String)
     * @see #setter(SetMethodSelector)
     * @since 4.0.0
     */
    @ExperimentalApi
    public static Selector setter(final Class<?> declaringClass, final String methodName) {
        return setter(declaringClass, methodName, null);
    }

    /**
     * Selects a setter method by name and parameter type in the specified class.
     *
     * <p><b>Note:</b> setter selectors can only be used if
     * {@link Keys#ASSIGNMENT_TYPE} is set to {@code AssignmentType.METHOD}.
     *
     * @param declaringClass class declaring the method
     * @param methodName     method name to select
     * @param parameterType  the parameter type of the setter
     * @return a selector for given method
     * @throws InstancioApiException if the class has no method with
     *                               the specified name and parameter type
     * @since 4.0.0
     */
    @ExperimentalApi
    public static Selector setter(final Class<?> declaringClass, final String methodName, final Class<?> parameterType) {
        ApiValidator.notNull(methodName, "method name must not be null");
        return SelectorImpl.builder()
                .target(new TargetSetterName(declaringClass, methodName, parameterType))
                .build();
    }

    /**
     * Selects a setter method based on the given method reference.
     *
     * <p>This selector resolves the class that declares the setter and
     * the method name from the method reference. Since this method resolves
     * the setter by name only (ignoring the parameter type)
     * it should only be used if there are no overloaded setters.
     *
     * <p><b>Note:</b> setter selectors can only be used if
     * {@link Keys#ASSIGNMENT_TYPE} is set to {@code AssignmentType.METHOD}.
     *
     * @param methodReference method reference from which the method will be resolved
     * @param <T>             type declaring the method
     * @param <U>             the argument type of the method
     * @return a method selector matching the given method reference
     * @see #setter(Class, String, Class)
     * @since 4.0.0
     */
    @ExperimentalApi
    public static <T, U> Selector setter(final SetMethodSelector<T, U> methodReference) {
        ApiValidator.notNull(methodReference, "setter method reference must not be null");
        return SelectorImpl.builder()
                .target(new TargetSetterReference(methodReference))
                .build();
    }

    /**
     * Selects the root object.
     *
     * @return the selector for the root object
     * @since 2.0.0
     */
    public static TargetSelector root() {
        return SelectorImpl.getRootSelector();
    }

    /**
     * Shorthand for {@code all(String.class)}.
     *
     * @return selector for all Strings
     * @since 1.2.0
     */
    public static Selector allStrings() {
        return all(String.class);
    }

    /**
     * Selects all bytes, primitive and wrapper.
     *
     * @return selector for all bytes
     * @since 1.2.0
     */
    public static Selector allBytes() {
        return new PrimitiveAndWrapperSelectorImpl(byte.class, Byte.class);
    }

    /**
     * Selects all floats, primitive and wrapper.
     *
     * @return selector for all floats
     * @since 1.2.0
     */
    public static Selector allFloats() {
        return new PrimitiveAndWrapperSelectorImpl(float.class, Float.class);
    }

    /**
     * Selects all shorts, primitive and wrapper.
     *
     * @return selector for all shorts
     * @since 1.2.0
     */
    public static Selector allShorts() {
        return new PrimitiveAndWrapperSelectorImpl(short.class, Short.class);
    }

    /**
     * Selects all integers, primitive and wrapper.
     *
     * @return selector for all integers
     * @since 1.2.0
     */
    public static Selector allInts() {
        return new PrimitiveAndWrapperSelectorImpl(int.class, Integer.class);
    }

    /**
     * Selects all longs, primitive and wrapper.
     *
     * @return selector for all longs
     * @since 1.2.0
     */
    public static Selector allLongs() {
        return new PrimitiveAndWrapperSelectorImpl(long.class, Long.class);
    }

    /**
     * Selects all doubles, primitive and wrapper.
     *
     * @return selector for all doubles
     * @since 1.2.0
     */
    public static Selector allDoubles() {
        return new PrimitiveAndWrapperSelectorImpl(double.class, Double.class);
    }

    /**
     * Selects all booleans, primitive and wrapper.
     *
     * @return selector for all booleans
     * @since 1.2.0
     */
    public static Selector allBooleans() {
        return new PrimitiveAndWrapperSelectorImpl(boolean.class, Boolean.class);
    }

    /**
     * Selects all characters, primitive and wrapper.
     *
     * @return selector for all characters
     * @since 1.2.0
     */
    public static Selector allChars() {
        return new PrimitiveAndWrapperSelectorImpl(char.class, Character.class);
    }

    /**
     * Creates a scope for narrowing down a selector's target to a field of the specified class.
     * <p>
     * For example, the following will set all lists within {@code Person.address} object to an empty list.
     *
     * <pre>{@code
     * Person person = Instancio.of(Person.class)
     *     .set(all(List.class).within(scope(Person.class, "address")), Collections.emptyList())
     *     .create();
     * }</pre>
     *
     * @param targetClass of the scope
     * @param fieldName   declared by the target class
     * @return a scope for fine-tuning a selector
     * @see #scope(GetMethodSelector)
     * @since 1.3.0
     */
    public static Scope scope(final Class<?> targetClass, final String fieldName) {
        return new ScopeImpl(new TargetFieldName(targetClass, fieldName), null);
    }

    /**
     * Creates a selector scope for narrowing down a selector's target to the specified class.
     * <p>
     * For example, assuming a {@code Customer} class that has a {@code CustomerConsent} class.
     * the following will set all booleans within {@code CustomerConsent} to {@code true}.
     *
     * <pre>{@code
     * Customer customer = Instancio.of(Customer.class)
     *     .set(allBooleans().within(scope(CustomerConsent.class)), true)
     *     .create();
     * }</pre>
     *
     * @param targetClass of the scope
     * @return a scope for fine-tuning a selector
     * @since 1.3.0
     */
    public static Scope scope(final Class<?> targetClass) {
        ApiValidator.notNull(targetClass, "Scope class must not be null");
        return new ScopeImpl(new TargetClass(targetClass), null);
    }

    /**
     * Creates a scope for narrowing down a selector's target to a
     * matching the specified method reference.
     *
     * <p>This is a convenience method for {@link #scope(Class, String)}
     * that avoids referring to a field by its name.
     *
     * @param methodReference method reference from which field name will be resolved
     * @param <T>             type declaring the method
     * @param <R>             return type of the method
     * @return a scope for fine-tuning a selector
     * @see #scope(Class, String)
     * @since 3.0.0
     */
    public static <T, R> Scope scope(final GetMethodSelector<T, R> methodReference) {
        ApiValidator.notNull(methodReference, "getter method reference must not be null");
        return new ScopeImpl(new TargetGetterReference(methodReference), null);
    }

    /**
     * Creates a scope from the given predicate selector.
     *
     * @param selector a predicate selector to use as the scope
     * @return a scope for fine-tuning a selector
     * @since 4.2.0
     */
    @ExperimentalApi
    public static Scope scope(final PredicateSelector selector) {
        return new PredicateScopeImpl(selector);
    }

    private Select() {
        // non-instantiable
    }

}