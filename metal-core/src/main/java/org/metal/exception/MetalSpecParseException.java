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

public class MetalSpecParseException extends MetalException {

  public MetalSpecParseException() {
  }

  public MetalSpecParseException(String message) {
    super(message);
  }

  public MetalSpecParseException(String message, String metal) {
    super(message, metal);
  }

  public MetalSpecParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalSpecParseException(String message, Throwable cause, String metal) {
    super(message, cause, metal);
  }

  public MetalSpecParseException(Throwable cause) {
    super(cause);
  }

  public MetalSpecParseException(Throwable cause, String metal) {
    super(cause, metal);
  }

  public MetalSpecParseException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public MetalSpecParseException(String message, Throwable cause, String metal,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, metal, enableSuppression, writableStackTrace);
  }
}

