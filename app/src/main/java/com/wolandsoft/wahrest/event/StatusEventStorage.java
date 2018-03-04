/*
    Copyright 2018 Alexander Shulgin

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.wolandsoft.wahrest.event;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Stores list of events happened since application start.
 *
 * @author Alexander Shulgin
 */
public class StatusEventStorage extends ArrayList<StatusEvent> {

    private static StatusEventStorage thisInstance;

    private LinkedList<OnStatusEventStorageChanges> mListeners = new LinkedList<>();
    private StatusEventStorage(){

    }

    public static StatusEventStorage getInstance(){
        if(thisInstance == null){
            thisInstance = new StatusEventStorage();
        }
        return thisInstance;
    }

    public void addOnStatusEventStorageChangesListener(OnStatusEventStorageChanges listener) {
        mListeners.add(listener);
    }

    @Override
    public boolean add(StatusEvent statusEvent) {
        boolean isAdded =  super.add(statusEvent);
        for(OnStatusEventStorageChanges nextListener : mListeners) {
            nextListener.onStatusEventAdded();
        }
        return isAdded;
    }
}
