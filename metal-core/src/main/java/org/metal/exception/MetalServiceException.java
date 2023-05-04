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

public class MetalServiceException extends MetalException {

  public MetalServiceException() {
  }

  public MetalServiceException(String message) {
    super(message);
  }

  public MetalServiceException(String message, String metal) {
    super(message, metal);
  }

  public MetalServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetalServiceException(String message, Throwable cause, String metal) {
    super(message, cause, metal);
  }

  public MetalServiceException(Throwable cause) {
    super(cause);
  }

  public MetalServiceException(Throwable cause, String metal) {
    super(cause, metal);
  }

  public MetalServiceException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public MetalServiceException(String message, Throwable cause, String metal,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, metal, enableSuppression, writableStackTrace);
  }
}

