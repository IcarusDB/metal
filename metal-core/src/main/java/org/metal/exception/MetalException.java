/*
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

package org.metal.exception;

public class MetalException extends RuntimeException {

  public MetalException() {
  }

  public MetalException(String message) {
    super(message);
  }

  public MetalException(String message, String metal) {
    super("Metal[" + metal + "]" + message);
  }

  public MetalException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalException(String message, Throwable cause, String metal) {
    super("Metal[" + metal + "]" + message, cause);
  }

  public MetalException(Throwable cause) {
    super(cause);
  }

  public MetalException(Throwable cause, String metal) {
    super("Metal[" + metal + "]", cause);
  }

  public MetalException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public MetalException(String message, Throwable cause, String metal, boolean enableSuppression,
      boolean writableStackTrace) {
    super("Metal[" + metal + "]" + message, cause, enableSuppression, writableStackTrace);
  }
}
