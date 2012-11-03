/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.authzStandardApiClientExt.net.sf.ezmorph;

/**
 * Marker interface for morphers that return an Object.<br>
 * 
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public interface ObjectMorpher extends Morpher
{
   /**
    * Morphs the input object into an output object of the supported type.
    * 
    * @param value The input value to be morphed
    * @exception MorphException if conversion cannot be performed successfully
    */
   Object morph( Object value );
}