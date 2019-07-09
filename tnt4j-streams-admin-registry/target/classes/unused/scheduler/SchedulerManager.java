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

package com.jkoolcloud.tnt4j.streams.registry.zoo.scheduler;

import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.jkoolcloud.tnt4j.core.OpLevel;

/**
 * The type Scheduler manager.
 */
public class SchedulerManager {

	/**
	 * The Scheduler.
	 */
	Scheduler scheduler;

	/**
	 * Instantiates a new Scheduler manager.
	 *
	 * @param scheduler
	 *            the scheduler
	 */
	public SchedulerManager(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * Schedule job.
	 *
	 * @param jobDetail
	 *            the job detail
	 * @param trigger
	 *            the trigger
	 */
	public void scheduleJob(JobDetail jobDetail, Trigger trigger) {
		try {
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			LoggerWrapper.addMessage(OpLevel.WARNING,
					String.format("%s \n %s", "Failed to add job to quartz scheduler", e.toString()));
		}
	}

	/**
	 * Start.
	 */
	public void start() {
		try {
			String.format("%s \n %s", "Failed to add job to quartz scheduler");
			scheduler.start();
		} catch (SchedulerException e) {
			LoggerWrapper.addMessage(OpLevel.WARNING,
					String.format("%s \n %s", "Failed to start quartz scheduler", e.toString()));
		}
	}

	/**
	 * Shutdown.
	 */
	public void shutdown() {
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			LoggerWrapper.addMessage(OpLevel.WARNING,
					String.format("%s \n %s", "Failed to shutdown quartz scheduler", e.toString()));
		}
	}
}
