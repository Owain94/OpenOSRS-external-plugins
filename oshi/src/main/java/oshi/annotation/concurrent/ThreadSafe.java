/*
 * MIT License
 *
 * Copyright (c) 2020-2021 The OSHI Project Contributors: https://github.com/oshi/oshi/graphs/contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package oshi.annotation.concurrent;

import java.lang.annotation.Documented;

/**
 * The presence of this annotation indicates that the author believes the class
 * to be thread-safe. As such, there should be no sequence of accessing the
 * public methods or fields that could put an instance of this class into an
 * invalid state, irrespective of any rearrangement of those operations by the
 * Java Runtime and without introducing any requirements for synchronization or
 * coordination by the caller/accessor.
 * <p>
 * This annotation is intended for internal use in OSHI as a temporary
 * workaround until it is available in {@code jakarta.annotations}.
 */
@Documented
public @interface ThreadSafe {
}
