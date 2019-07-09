/*
 * Copyright 2014-2019 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unused;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The type Task manager.
 */
public class TaskManager {

	private Map<String, Timer> stringToTimerMap;

	/**
	 * Instantiates a new Task manager.
	 *
	 * @param stringToTimerMap
	 *            the string to timer map
	 */
	public TaskManager(Map<String, Timer> stringToTimerMap) {
		this.stringToTimerMap = stringToTimerMap;
	}

	/**
	 * Create timer.
	 *
	 * @param name
	 *            the name
	 */
	public void createTimer(String name) {
		stringToTimerMap.put(name, new Timer(name));
	}

	/**
	 * Give timer task.
	 *
	 * @param name
	 *            the name
	 * @param timerTask
	 *            the timer task
	 * @param ms
	 *            the ms
	 */
	public void giveTimerTask(String name, TimerTask timerTask, long ms) {
		stringToTimerMap.get(name).schedule(timerTask, 0, ms);
	}

	/**
	 * Stop timer.
	 *
	 * @param name
	 *            the name
	 */
	public void stopTimer(String name) {
		stringToTimerMap.get(name).cancel();
	}

	/**
	 * Purge.
	 *
	 * @param name
	 *            the name
	 */
	public void purge(String name) {
		stringToTimerMap.get(name).purge();
	}

	/**
	 * Stop all timers.
	 */
	public void stopAllTimers() {
		for (String timerName : stringToTimerMap.keySet()) {
			stopTimer(timerName);
			purge(timerName);
		}
	}

}
