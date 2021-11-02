/*
 * ao-tlds - Self-updating Java API to get top-level domains.
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-tlds.
 *
 * ao-tlds is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-tlds is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-tlds.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aoapps.tlds;

import com.aoapps.collections.AoCollections;
import com.aoapps.lang.io.IoUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Provides access to the current top level domains.
 * As a fall-back, a copy of the top-level domain list is bundled within this API.
 * The list is self-updated from <a href="https://data.iana.org/TLD/tlds-alpha-by-domain.txt">data.iana.org</a>.
 * Updates are persisted via the <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/preferences/">Java Preferences API</a>.
 *
 * @author  AO Industries, Inc.
 */
public class TopLevelDomain {

	private static final Logger logger = Logger.getLogger(TopLevelDomain.class.getName());

	/**
	 * Hard-coded bootstrap data.  This is used when no download completed or possible.
	 * This matches the bundled copy of tlds-alpha-by-domain.txt
	 * <pre>date +%s000L -d "Sun Jul  4 07:07:01 2021 UTC"</pre>
	 */
	private static final long LAST_UPDATED = 1625382421000L;

	/**
	 * In DEBUG mode, times are greatly shortened for testing.
	 */
	private static final boolean DEBUG = false;

	/**
	 * The URL accessed to update the list.
	 */
	private static final URL DATA_URL;
	static {
		try {
			DATA_URL = new URL("https://data.iana.org/TLD/tlds-alpha-by-domain.txt");
		} catch(MalformedURLException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/**
	 * The default encoding for the data url.
	 */
	private static final Charset DATA_ENCODING = StandardCharsets.UTF_8;

	/**
	 * One snapshot of the data, representing the state at one moment in time.
	 */
	public static class Snapshot {

		/**
		 * The minimum number of milliseconds between updates after a success.
		 */
		private static final long UPDATE_INTERVAL_SUCCESS_MIN =
			DEBUG
			? (         60L * 60 * 1000) // 1 hour
			: (7L * 24 * 60 * 60 * 1000) // 7 days
		;

		/**
		 * The randomized offset number of milliseconds between updates after a success.
		 */
		private static final int UPDATE_INTERVAL_SUCCESS_DEVIATION =
			DEBUG
			? (      5 * 60 * 1000) // 5 minutes
			: (24 * 60 * 60 * 1000) // 1 day
		;

		/**
		 * The minimum number of milliseconds between updates after a failure.
		 */
		private static final long UPDATE_INTERVAL_FAILURE_MIN =
			DEBUG
			? (     10L * 60 * 1000) // 10 minutes
			: (24L * 60 * 60 * 1000) // 1 day
		;

		/**
		 * The randomized offset number of milliseconds between updates after a failure.
		 */
		private static final int UPDATE_INTERVAL_FAILURE_DEVIATION =
			DEBUG
			? (         60 * 1000) // 1 minute
			: (4 * 60 * 60 * 1000) // 4 hours
		;

		private static final Preferences prefs = Preferences.userNodeForPackage(Snapshot.class); // systemNodeForPackage not available as regular user in Linux

		/**
		 * A fast pseudo-random number generator for non-cryptographic purposes.
		 */
		private static final Random fastRandom = new Random(IoUtils.bufferToLong(new SecureRandom().generateSeed(Long.BYTES)));

		private final String source;

		private final List<String> topLevelDomains;

		private final List<String> comments;

		private final long lastUpdatedTime;

		private final boolean isBootstrap;

		private final boolean lastUpdateSuccessful;

		private final long lastSuccessfulUpdateTime;

		/**
		 * MD5 sum to make sure we have a consistent snapshot from preferences.
		 * The preferences API is not atomic so inconsistent states are detected and ignored.
		 */
		private final byte[] md5sum;

		/**
		 * The next time the data will be updated.
		 */
		private final long nextUpdateAfter;

		/**
		 * Bounded time to handle extreme clock changes into the past.
		 */
		private final long nextUpdateBefore;

		private final Map<String, String> lowerTldMap;

		private Snapshot(
			String source,
			long lastUpdatedTime,
			boolean isBootstrap,
			boolean lastUpdateSuccessful,
			long lastSuccessfulUpdateTime
		) throws IOException {
			this.source = source;
			ArrayList<String> newTopLevelDomains = new ArrayList<>();
			ArrayList<String> newComments = new ArrayList<>();
			{
				BufferedReader in = new BufferedReader(new StringReader(source));
				String line;
				while((line = in.readLine()) != null) {
					if(line.startsWith("#")) {
						newComments.add(line);
					} else {
						newTopLevelDomains.add(line.intern());
					}
				}
				newTopLevelDomains.trimToSize();
				newComments.trimToSize();
			}
			this.topLevelDomains = Collections.unmodifiableList(newTopLevelDomains);
			this.comments = Collections.unmodifiableList(newComments);
			this.lastUpdatedTime = lastUpdatedTime;
			this.isBootstrap = isBootstrap;
			this.lastUpdateSuccessful = lastUpdateSuccessful;
			this.lastSuccessfulUpdateTime = lastSuccessfulUpdateTime;
			// Compute the MD5 sum
			{
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				try (DataOutputStream out = new DataOutputStream(bout)) {
					out.write(source.getBytes(DATA_ENCODING));
					out.writeLong(lastUpdatedTime);
					out.writeBoolean(lastUpdateSuccessful);
					out.writeLong(lastSuccessfulUpdateTime);
				}
				try {
					MessageDigest md = MessageDigest.getInstance("MD5");
					this.md5sum = md.digest(bout.toByteArray());
				} catch(NoSuchAlgorithmException e) {
					throw new AssertionError("MD5 is expected to be available on all platforms", e);
				}
			}
			// Random next update time
			{
				long updateMin;
				int updateDeviation;
				if(lastUpdateSuccessful) {
					updateMin = UPDATE_INTERVAL_SUCCESS_MIN;
					updateDeviation = UPDATE_INTERVAL_SUCCESS_DEVIATION;
				} else {
					updateMin = UPDATE_INTERVAL_FAILURE_MIN;
					updateDeviation = UPDATE_INTERVAL_FAILURE_DEVIATION;
				}
				int randomDeviation = fastRandom.nextInt(updateDeviation);
				this.nextUpdateAfter = lastUpdatedTime + updateMin + randomDeviation;
				this.nextUpdateBefore = lastUpdatedTime - updateMin - randomDeviation;
				if(logger.isLoggable(Level.FINE)) {
					logger.fine("updateMin=" + updateMin);
					logger.fine("updateDeviation=" + updateDeviation);
					logger.fine("randomDeviation=" + randomDeviation);
					logger.fine("nextUpdateAfter=" + new Date(nextUpdateAfter));
					logger.fine("nextUpdateBefore=" + new Date(nextUpdateBefore));
				}
			}
			// Compute lowerTldMap
			{
				lowerTldMap = AoCollections.newHashMap(topLevelDomains.size());
				for(String tld : topLevelDomains) {
					lowerTldMap.put(tld.toLowerCase(Locale.ROOT).intern(), tld);
				}
			}
		}

		/**
		 * Loads this snapshot from the system preferences.
		 *
		 * @return  the last stored snapshot (even if stored by a different process) or {@code null} if none available.
		 */
		@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
		private static Snapshot loadFromPreferences() {
			logger.fine("Loading from preferences");
			String source;
			{
				int numChunks = prefs.getInt("TopLevelDomain.source.numChunks", Integer.MIN_VALUE);
				if(numChunks == Integer.MIN_VALUE) {
					logger.fine("Not found in preferences");
					return null;
				}
				if(logger.isLoggable(Level.FINE)) logger.fine("numChunks="+numChunks);
				StringBuilder sourceSB = new StringBuilder(numChunks * Preferences.MAX_VALUE_LENGTH);
				for(int i=0; i<numChunks; i++) {
					String chunk = prefs.get("TopLevelDomain.source." + i, null);
					if(chunk == null) {
						if(logger.isLoggable(Level.WARNING)) {
							logger.log(Level.WARNING, "Unable to load top level domains from preferences, chunk missing: " + i);
						}
						return null;
					}
					sourceSB.append(chunk);
				}
				source = sourceSB.toString();
			}
			long lastUpdatedTime = prefs.getLong("TopLevelDomain.lastUpdatedTime", Long.MIN_VALUE);
			boolean lastUpdateSuccessful = prefs.getBoolean("TopLevelDomain.lastUpdateSuccessful", false);
			long lastSuccessfulUpdateTime = prefs.getLong("TopLevelDomain.lastSuccessfulUpdateTime", Long.MIN_VALUE);
			if(logger.isLoggable(Level.FINE)) {
				logger.fine("lastUpdatedTime="+new Date(lastUpdatedTime));
				logger.fine("lastUpdateSuccessful="+lastUpdateSuccessful);
				logger.fine("lastSuccessfulUpdateTime="+new Date(lastSuccessfulUpdateTime));
			}
			byte[] md5sum = prefs.getByteArray("TopLevelDomain.md5sum", null);
			if(
				lastUpdatedTime != Long.MIN_VALUE
				&& lastSuccessfulUpdateTime != Long.MIN_VALUE
				&& md5sum != null
			) {
				try {
					Snapshot newSnapshot = new Snapshot(source, lastUpdatedTime, false, lastUpdateSuccessful, lastSuccessfulUpdateTime);
					if(!Arrays.equals(md5sum, newSnapshot.md5sum)) {
						logger.log(Level.WARNING, "Unable to load top level domains from preferences, ignoring: md5sum mismatch");
						return null;
					}
					logger.fine("Successful load from preferences");
					return newSnapshot;
				} catch(ThreadDeath td) {
					throw td;
				} catch(Throwable t) {
					logger.log(Level.SEVERE, "Unable to load top level domains from preferences", t);
					return null;
				}
			} else {
				if(logger.isLoggable(Level.WARNING)) logger.warning("Incomplete data in preferences, ignoring");
				return null;
			}
		}

		/**
		 * Loads this snapshot from the provided reader.
		 */
		private static Snapshot loadFromReader(
			Reader in,
			long lastUpdatedTime,
			boolean isBootstrap
		) throws IOException {
			StringBuilder sb = new StringBuilder();
			char[] buff = new char[4096];
			int numChars;
			while((numChars = in.read(buff)) != -1) {
				sb.append(buff, 0, numChars);
			}
			return new Snapshot(sb.toString(), lastUpdatedTime, isBootstrap, true, lastUpdatedTime);
		}

		/**
		 * Stores this snapshot to the system preferences.
		 */
		private void saveToPreferences() throws BackingStoreException {
			logger.fine("Saving to preferences");
			int numChunks = 0;
			for(int pos=0, len=source.length(); pos<len; pos+=Preferences.MAX_VALUE_LENGTH) {
				prefs.put(
					"TopLevelDomain.source." + numChunks++,
					source.substring(
						pos,
						Math.min(len, pos + Preferences.MAX_VALUE_LENGTH)
					)
				);
			}
			prefs.putInt("TopLevelDomain.source.numChunks", numChunks);
			prefs.putLong("TopLevelDomain.lastUpdatedTime", lastUpdatedTime);
			prefs.putBoolean("TopLevelDomain.lastUpdateSuccessful", lastUpdateSuccessful);
			prefs.putLong("TopLevelDomain.lastSuccessfulUpdateTime", lastSuccessfulUpdateTime);
			prefs.putByteArray("TopLevelDomain.md5sum", md5sum);
			logger.fine("Flushing preferences");
			prefs.flush();
		}

		/**
		 * @see  TopLevelDomain#getTopLevelDomains()
		 */
		@SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmod
		public List<String> getTopLevelDomains() {
			return topLevelDomains;
		}

		/**
		 * @see  TopLevelDomain#getComments()
		 */
		@SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmod
		public List<String> getComments() {
			return comments;
		}

		/**
		 * @see  TopLevelDomain#getLastUpdatedTime()
		 */
		public long getLastUpdatedTime() {
			return lastUpdatedTime;
		}

		/**
		 * @see  TopLevelDomain#isBootstrap()
		 */
		public boolean isBootstrap() {
			return isBootstrap;
		}

		/**
		 * @see  TopLevelDomain#getLastUpdateSuccessful()
		 */
		public boolean getLastUpdateSuccessful() {
			return lastUpdateSuccessful;
		}

		/**
		 * @see  TopLevelDomain#getLastSuccessfulUpdateTime()
		 */
		public long getLastSuccessfulUpdateTime() {
			return lastSuccessfulUpdateTime;
		}

		/**
		 * @see  TopLevelDomain#getByLabel(java.lang.String)
		 */
		public String getByLabel(String label) {
			return lowerTldMap.get(label.toLowerCase(Locale.ROOT));
		}
	}

	/**
	 * Lock for snapshot.
	 */
	private static class Lock {}
	private static final Lock lock = new Lock();

	/**
	 * The last obtained snapshot.
	 */
	private static Snapshot snapshot;

	/**
	 * The thread currently doing a background update, if any.
	 */
	private static Thread updateThread;

	/**
	 * Gets a snapshot of the current set of top-level domains, in the case and order contained within
	 * <a href="https://data.iana.org/TLD/tlds-alpha-by-domain.txt">tlds-alpha-by-domain.txt</a>.
	 * Will trigger asynchronous background update if it is time to to so, but will use the currently
	 * available data and not wait for the update to complete.
	 */
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	public static Snapshot getSnapshot() {
		synchronized(lock) {
			if(snapshot == null) {
				// Load from preferences
				logger.fine("Trying to load from preferences");
				snapshot = Snapshot.loadFromPreferences();
				// Use hard-coded bootstrap
				if(snapshot == null || snapshot.lastSuccessfulUpdateTime < LAST_UPDATED) {
					if(logger.isLoggable(Level.INFO)) {
						DateFormat dateFormat = DateFormat.getDateTimeInstance();
						if(snapshot == null) {
							logger.info(
								"Update not found in preferences, using hard-coded bootstrap dated \""
								+ dateFormat.format(new Date(LAST_UPDATED))
								+ "\""
							);
						} else {
							assert snapshot.lastSuccessfulUpdateTime < LAST_UPDATED;
							logger.info(
								"Update from preferences dated \""
								+ dateFormat.format(new Date(snapshot.lastSuccessfulUpdateTime))
								+ "\" is older than hard-coded bootstrap dated \""
								+ dateFormat.format(new Date(LAST_UPDATED))
								+ "\", using hard-coded bootstrap instead"
							);
						}
					}
					try {
						try (Reader in = new InputStreamReader(TopLevelDomain.class.getResourceAsStream("tlds-alpha-by-domain.txt"), DATA_ENCODING)) {
							snapshot = Snapshot.loadFromReader(in, LAST_UPDATED, true);
						}
					} catch(IOException e) {
						throw new UncheckedIOException("Unable to load bootstrap top level domains", e);
					}
				} else {
					logger.fine("Successfully loaded from preferences");
				}
			}
			// Trigger background update if is time
			if(updateThread == null) {
				final long currentTime = System.currentTimeMillis();
				if(
					currentTime >= snapshot.nextUpdateAfter
					|| currentTime <= snapshot.nextUpdateBefore
				) {
					if(logger.isLoggable(Level.FINE)) {
						logger.fine("Time for background update: currentTime=" + new Date(currentTime)
							+ ", nextUpdateAfter=" + new Date(snapshot.nextUpdateAfter)
							+ ", nextUpdateBefore=" + new Date(snapshot.nextUpdateBefore)
						);
					}
					// Reload from preferences, just in case another process has already updated
					logger.fine("Reloading from preferences before beginning background update");
					Snapshot newSnapshot = Snapshot.loadFromPreferences();
					if(
						newSnapshot != null
						&& snapshot.lastUpdatedTime != newSnapshot.lastUpdatedTime
						&& currentTime < newSnapshot.nextUpdateAfter
						&& currentTime > newSnapshot.nextUpdateBefore
					) {
						// newSnapshot is valid, use it
						logger.fine("Update from preferences is current, using it instead of beginning background update");
						snapshot = newSnapshot;
					} else {
						// Begin background update
						logger.fine("Spawning background update thread");
						updateThread = new Thread(
							() -> {
								try {
									logger.fine("Connecting to " + DATA_URL);
									URLConnection conn = DATA_URL.openConnection();
									String encoding = conn.getContentEncoding();
									if(encoding == null) {
										logger.fine("Did not get encoding, assuming encoding: " + DATA_ENCODING);
										encoding = DATA_ENCODING.name();
									} else {
										if(logger.isLoggable(Level.FINE)) logger.fine("Got encoding: " + encoding);
									}
									logger.fine("Getting input");
									Reader in = new InputStreamReader(conn.getInputStream(), encoding);
									try {
										logger.fine("Reading top level domains from input");
										Snapshot loadedSnapshot = Snapshot.loadFromReader(in, currentTime, false);
										synchronized(lock) {
											snapshot = loadedSnapshot;
											try {
												logger.fine("Saving updated top level domains to preferences");
												snapshot.saveToPreferences();
											} catch(BackingStoreException e) {
												logger.log(Level.SEVERE, "Unable to save new snapshot to preferences", e);
											}
										}
									} finally {
										logger.fine("Closing input");
										in.close();
									}
								} catch(ThreadDeath td) {
									throw td;
								} catch(Throwable t) {
									logger.log(Level.SEVERE, "Unable to load new snapshot", t);
									try {
										synchronized(lock) {
											logger.fine("Saving failed update of top level domains to preferences");
											snapshot = new Snapshot(
												snapshot.source,
												currentTime,
												false,
												false,
												snapshot.lastSuccessfulUpdateTime
											);
											try {
												snapshot.saveToPreferences();
											} catch(BackingStoreException e2) {
												logger.log(Level.SEVERE, "Unable to save new snapshot to preferences", e2);
											}
										}
									} catch(IOException e2) {
										logger.log(Level.SEVERE, "Unable to update existing snapshot to unsuccessful", e2);
									}
								} finally {
									synchronized(lock) {
										updateThread = null;
										lock.notifyAll();
									}
								}
							},
							TopLevelDomain.class.getName() + ".updateThread"
						);
						updateThread.start();
					}
				}
			}
			return snapshot;
		}
	}

	/**
	 * Gets an unmodifiable list of the most recently retrieved top-level domains,
	 * in the case and order contained within
	 * <a href="https://data.iana.org/TLD/tlds-alpha-by-domain.txt">tlds-alpha-by-domain.txt</a>.
	 * <p>
	 * Each element is {@link String#intern() interned}.
	 * </p>
	 *
	 * @see  Snapshot#getTopLevelDomains()
	 * @see  #getSnapshot()
	 */
	public static List<String> getTopLevelDomains() {
		return getSnapshot().getTopLevelDomains();
	}

	/**
	 * Gets an unmodifiable list of the comments contained within
	 * <a href="https://data.iana.org/TLD/tlds-alpha-by-domain.txt">tlds-alpha-by-domain.txt</a>.
	 * All lines starting with {@code "#"} are considered to be comments.
	 *
	 * @see  Snapshot#getComments()
	 * @see  #getSnapshot()
	 */
	public static List<String> getComments() {
		return getSnapshot().getComments();
	}

	/**
	 * Gets the last time the list was updated, whether
	 * successful or not.
	 *
	 * @see  Snapshot#getLastUpdatedTime()
	 * @see  #getSnapshot()
	 */
	public static long getLastUpdatedTime() {
		return getSnapshot().getLastUpdatedTime();
	}

	/**
	 * Gets whether or not this is the bundled bootstrap data.
	 *
	 * @return  {@code true} if this is the included bootstrap data, or {@code false} is this is auto-updated
	 *
	 * @see  Snapshot#isBootstrap()
	 * @see  #getSnapshot()
	 */
	public static boolean isBootstrap() {
		return getSnapshot().isBootstrap();
	}

	/**
	 * Gets whether the last update was successful.
	 *
	 * @see  Snapshot#getLastUpdateSuccessful()
	 * @see  #getSnapshot()
	 */
	public static boolean getLastUpdateSuccessful() {
		return getSnapshot().getLastUpdateSuccessful();
	}

	/**
	 * Gets the last time the list was successfully updated.
	 *
	 * @see  Snapshot#getLastSuccessfulUpdateTime()
	 * @see  #getSnapshot()
	 */
	public static long getLastSuccessfulUpdateTime() {
		return getSnapshot().getLastSuccessfulUpdateTime();
	}

	/**
	 * Provides a way to get the top level domain based on label (case-insensitive).
	 * <p>
	 * Any non-null returned value is {@link String#intern() interned}.
	 * </p>
	 *
	 * @return  The top level domain based on label (case-insensitive) or {@code null} if no match.
	 *
	 * @see  Snapshot#getByLabel(java.lang.String)
	 * @see  #getSnapshot()
	 */
	public static String getByLabel(String label) {
		return getSnapshot().getByLabel(label);
	}

	/**
	 * For interaction with testing, waits until no thread running.
	 */
	static void waitUntilNoThread() throws InterruptedException {
		synchronized(lock) {
			if(updateThread != null) {
				logger.info("Waiting for background update to complete");
				do {
					lock.wait();
				} while(updateThread != null);
				logger.info("Background update completed");
			}
		}
	}

	/**
	 * Make no instances.
	 */
	private TopLevelDomain() {
	}
}
