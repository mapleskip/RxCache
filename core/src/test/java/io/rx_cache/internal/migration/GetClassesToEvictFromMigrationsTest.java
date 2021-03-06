/*
 * Copyright 2015 Victor Albertos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rx_cache.internal.migration;


import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import io.rx_cache.Migration;
import io.rx_cache.SchemeMigration;
import rx.observers.TestSubscriber;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GetClassesToEvictFromMigrationsTest {
    private GetClassesToEvictFromMigrations getClassesToEvictFromMigrationsUT;
    private TestSubscriber<List<Class>> testSubscriber;

    @Before public void setUp() {
        getClassesToEvictFromMigrationsUT = new GetClassesToEvictFromMigrations();
        testSubscriber = new TestSubscriber<>();
    }

    @Test public void When_Migration_Contain_One_Class_To_Evict_Get_It() {
        Annotation annotation = OneMigrationProviders.class.getAnnotation(SchemeMigration.class);
        SchemeMigration schemeMigration = (SchemeMigration) annotation;
        List<Migration> migrations = Arrays.asList(schemeMigration.value());

        getClassesToEvictFromMigrationsUT.with(migrations).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<Class> classes = testSubscriber.getOnNextEvents().get(0);
        assertThat(classes.size(), is(1));
    }

    @Test public void When_Migrations_Contains_Classes_To_Evict_Get_Them() {
        Annotation annotation = MigrationsProviders.class.getAnnotation(SchemeMigration.class);
        SchemeMigration schemeMigration = (SchemeMigration) annotation;
        List<Migration> migrations = Arrays.asList(schemeMigration.value());

        getClassesToEvictFromMigrationsUT.with(migrations).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<Class> classes = testSubscriber.getOnNextEvents().get(0);
        assertThat(classes.size(), is(2));
    }

    @Test public void When_Several_Classes_To_Evict_With_Same_Type_Only_Keep_One() {
        Annotation annotation = MigrationsRepeatedProviders.class.getAnnotation(SchemeMigration.class);
        SchemeMigration schemeMigration = (SchemeMigration) annotation;
        List<Migration> migrations = Arrays.asList(schemeMigration.value());

        getClassesToEvictFromMigrationsUT.with(migrations).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<Class> classes = testSubscriber.getOnNextEvents().get(0);
        assertThat(classes.size(), is(3));
    }

    @SchemeMigration(@Migration(version = 1, evictClasses = {Mock1.class}))
    private interface OneMigrationProviders {}


    @SchemeMigration({
            @Migration(version = 1, evictClasses = {Mock1.class}),
            @Migration(version = 2, evictClasses = {Mock2.class}),
    })
    private interface MigrationsProviders {}


    @SchemeMigration({
            @Migration(version = 1, evictClasses = {Mock1.class}),
            @Migration(version = 2, evictClasses = {Mock2.class}),
            @Migration(version = 3, evictClasses = {Mock1.class}),
            @Migration(version = 4, evictClasses = {Mock2.class}),
            @Migration(version = 5, evictClasses = {Mock3.class})
    })
    private interface MigrationsRepeatedProviders {}

    private class Mock1 {}
    private class Mock2 {}
    private class Mock3 {}
}
