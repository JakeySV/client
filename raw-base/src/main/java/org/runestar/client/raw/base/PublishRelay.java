/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package org.runestar.client.raw.base;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class PublishRelay<T> extends Observable<T> implements Consumer<T> {

    private static final VarHandle SUBSCRIBERS;
    static {
        try {
            SUBSCRIBERS = MethodHandles.lookup().findVarHandle(PublishRelay.class, "subscribers", PublishDisposable[].class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /** An empty subscribers array to avoid allocating it all the time. */
    @SuppressWarnings("rawtypes")
    static final PublishDisposable[] EMPTY = new PublishDisposable[0];

    /** The array of currently subscribed subscribers. */
    volatile PublishDisposable<T>[] subscribers;

    /**
     * Constructs a PublishRelay.
     */
    @SuppressWarnings("unchecked")
    PublishRelay() {
        subscribers = EMPTY;
    }

    @Override
    protected void subscribeActual(Observer<? super T> t) {
        PublishDisposable<T> ps = new PublishDisposable<T>(t, this);
        t.onSubscribe(ps);
        add(ps);
        // if cancellation happened while a successful add, the remove() didn't work
        // so we need to do it again
        if (ps.isDisposed()) {
            remove(ps);
        }
    }

    /**
     * Adds the given subscriber to the subscribers array atomically.
     * @param ps the subscriber to add
     */
    void add(PublishDisposable<T> ps) {
        for (;;) {
            PublishDisposable<T>[] a = subscribers;
            int n = a.length;
            @SuppressWarnings("unchecked")
            PublishDisposable<T>[] b = new PublishDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = ps;

            if (SUBSCRIBERS.compareAndSet(this, a, b)) {
                return;
            }
        }
    }

    /**
     * Atomically removes the given subscriber if it is subscribed to the subject.
     * @param ps the subject to remove
     */
    @SuppressWarnings("unchecked")
    void remove(PublishDisposable<T> ps) {
        for (;;) {
            PublishDisposable<T>[] a = subscribers;
            if (a == EMPTY) {
                return;
            }

            int n = a.length;
            int j = -1;
            for (int i = 0; i < n; i++) {
                if (a[i] == ps) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }

            PublishDisposable<T>[] b;

            if (n == 1) {
                b = EMPTY;
            } else {
                b = new PublishDisposable[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (SUBSCRIBERS.compareAndSet(this, a, b)) {
                return;
            }
        }
    }

    @Override
    public void accept(T value) {
        Objects.requireNonNull(value);
        for (PublishDisposable<T> s : subscribers) {
            s.onNext(value);
        }
    }

    public boolean hasObservers() {
        return subscribers != EMPTY;
    }

    /**
     * Wraps the actual subscriber, tracks its requests and makes cancellation
     * to remove itself from the current subscribers array.
     *
     * @param <T> the value type
     */
    static final class PublishDisposable<T> extends AtomicBoolean implements Disposable {

        /** The actual subscriber. */
        final Observer<? super T> downstream;

        /** The subject state. */
        final PublishRelay<T> parent;

        /**
         * Constructs a PublishSubscriber, wraps the actual subscriber and the state.
         * @param actual the actual subscriber
         * @param parent the parent PublishProcessor
         */
        PublishDisposable(Observer<? super T> actual, PublishRelay<T> parent) {
            this.downstream = actual;
            this.parent = parent;
        }

        public void onNext(T t) {
            if (!get()) {
                downstream.onNext(t);
            }
        }

        @Override
        public void dispose() {
            if (compareAndSet(false, true)) {
                parent.remove(this);
            }
        }

        @Override
        public boolean isDisposed() {
            return get();
        }
    }
}