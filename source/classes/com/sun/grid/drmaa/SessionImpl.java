/*___INFO__MARK_BEGIN__*/
/*************************************************************************
 *
 *  The Contents of this file are made available subject to the terms of
 *  the Sun Industry Standards Source License Version 1.2
 *
 *  Sun Microsystems Inc., March, 2001
 *
 *
 *  Sun Industry Standards Source License Version 1.2
 *  =================================================
 *  The contents of this file are subject to the Sun Industry Standards
 *  Source License Version 1.2 (the "License"); You may not use this file
 *  except in compliance with the License. You may obtain a copy of the
 *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
 *
 *  Software provided under this License is provided on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
 *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 *  See the License for the specific provisions governing your rights and
 *  obligations concerning the Software.
 *
 *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
 *
 *   Copyright: 2001 by Sun Microsystems, Inc.
 *
 *   All Rights Reserved.
 *
 ************************************************************************/
/*___INFO__MARK_END__*/
package com.sun.grid.drmaa;

import java.security.*;
import java.util.*;

import org.ggf.drmaa.*;

/** <p>The SessionImpl provides a DRMAA interface to Grid Engine.  This interface
 * is built on top of the DRMAA C binding using JNI.  In order to keep the
 * native code as localized as possible, this class also provides native DRMAA
 * services to other classes, such as the JobTemplateImpl.</p>
 * <p>This class relies on the <i>drmaa</i> shared library.</p>
 * @see org.ggf.drmaa.Session
 * @see com.sun.grid.drmaa.JobTemplateImpl
 * @see <a href="http://gridengine.sunsource.net/unbranded-source/browse/~checkout~/gridengine/doc/htmlman/manuals.html?content-type=text/html">Grid Engine Man Pages</a>
 * @author dan.templeton@sun.com
 * @since 0.5
 */
public class SessionImpl implements Session {
   private String sessionId = null;
   
   static {
      AccessController.doPrivileged (new PrivilegedAction () {
         public Object run () {
            System.loadLibrary ("drmaa");
            return null;
         }
      });
   }
   
   /** Creates a new instance of SessionImpl */
   SessionImpl () {
   }
   
   /** <p>Hold, release, suspend, resume, or kill the job identified by jobId.
    * If jobId is <code>JOB_IDS_SESSION_ALL</code>, then this routine acts on all jobs
    * <B>submitted</B> during this DRMAA session up to the moment control() is
    * called.  To avoid race conditions in multithreaded applications, the DRMAA
    * implementation user should explicitly synchronize this call with any other
    * job submission calls or control calls that may change the number of remote
    * jobs.</p>
    * <p>The legal values for action and their meanings are:</p>
    * <UL>
    * <LI><code>SUSPEND</code>: stop the job,</LI>
    * <LI><code>RESUME</code>: (re)start the job,</LI>
    * <LI><code>HOLD</code>: put the job on-hold,</LI>
    * <LI><code>RELEASE</code>: release the hold on the job, and</LI>
    * <LI><code>TERMINATE</code>: kill the job.</LI>
    * </UL>
    * <p>This routine returns once the action has been acknowledged by
    * the DRM system, but does not necessarily wait until the action
    * has been completed.</p>
    * <p>The DRMAA suspend/resume operations are equivalent to the use of
    * `-s <jobid>' and `-us <jobid>' options with qmod.  (See the qmod(1) man
    * page.)</p>
    * <p>The DRMAA hold/release operations are equivalent to the use of
    * qhold and qrls.  (See the qhold(1) and qrls(1) man pages.)</p>
    * <p>The DRMAA terminate operation is equivalent to the use of qdel.  (See
    * the qdel(1) man page.)</p>
    * <p>Only user hold and user suspend can be controled via control().  For
    * affecting system hold and system suspend states the appropriate DRM
    * interfaces must be used.</p>
    * @param jobId The id of the job to control
    * @param action the control action to be taken
    * @throws DrmaaException May be one of the following:
    * <UL>
    * <LI>DrmCommunicationException</LI>
    * <LI>AuthorizationException</LI>
    * <LI>ResumeInconsistentStateException</LI>
    * <LI>SuspendInconsistentStateException</LI>
    * <LI>HoldInconsistentStateException</LI>
    * <LI>ReleaseInconsistentStateException</LI>
    * <LI>InvalidJobException</LI>
    * </UL>
    */
   public void control (String jobId, int action) throws DrmaaException {
      this.nativeControl (jobId, action);
   }
   
   private native void nativeControl (String jobId, int action) throws DrmaaException;
   
   /** <p>The exit() method closes the DRMAA session for all threads and must be
    * called before process termination.  The exit() method may be called only
    * once by a single thread in the process and may only be called after the
    * init() function has completed.  Any call to exit() before init() returns
    * or after exit() has already been called will result in a
    * NoActiveSessionException.</p>
    * <p>The exit() method does neccessary clean up of the DRMAA session state,
    * including unregistering from the qmaster.  If the exit() method is not
    * called, the qmaster will store events for the DRMAA client until the
    * connection times out, causing extra work for the qmaster and comsuming
    * system resources.</p>
    * <p>Submitted jobs are not affected by the exit() method.</p>
    * @throws DrmaaException May be one of the following:
    * <UL>
    * <LI>DrmsExitException</LI>
    * <LI>NoActiveSessionException</LI>
    * </UL>
    */
   public void exit () throws DrmaaException {
      this.nativeExit ();
   }
   
   private native void nativeExit () throws DrmaaException;
   
   /**
    * <p>getContact() returns an opaque string containing contact information
    * related to the current DRMAA session to be used with the init() method.
    * The contact string takes the form <code>name=value[;name=value]*</code>,
    * where name and value are both strings, and the supported values of name
    * are:</p>
    * <ul>
    *    <li><code>session</code> - if used, indicates to which session id
    *        to reconnect.</li>
    * </ul>
    * <p>Before the init() method has been called, this method will always
    * return an empty string.  After the init() method has been called, this
    * method will return the set of name=value pairs which represent the
    * currently active session.  The value returned for <code>session</code>
    * can be used with the init() method to reconnect to the current session
    * after exit() has been called.</p>
    * @return current contact information for DRM system or a comma delimited
    * list of possible contact Strings
    * @see #init(String)
    */
   public String getContact () {
      return this.nativeGetContact ();
   }
   
   private native String nativeGetContact ();
   
   /** The getDRMSystem() method returns a string containing the DRM product and
    * version information.  The getDRMSystem() function returns the same value
    * before and after init() is called.
    * @return DRM system implementation information
    */
   public String getDrmSystem () {
      return this.nativeGetDRMSInfo ();
   }
   
   private native String nativeGetDRMSInfo ();
   
   /** <p>Get the program status of the job identified by jobId.
    * The possible return values and their meanings are:</p>
    * <UL>
    * <LI><code>UNDETERMINED</code>: process status cannot be determined</LI>
    * <LI><code>QUEUED_ACTIVE</code>: job is queued and active</LI>
    * <LI><code>SYSTEM_ON_HOLD</code>: job is queued and in system hold</LI>
    * <LI><code>USER_ON_HOLD</code>: job is queued and in user hold</LI>
    * <LI><code>USER_SYSTEM_ON_HOLD</code>: job is queued and in user and system hold</LI>
    * <LI><code>RUNNING</code>: job is running</LI>
    * <LI><code>SYSTEM_SUSPENDED</code>: job is system suspended</LI>
    * <LI><code>USER_SUSPENDED</code>: job is user suspended</LI>
    * <LI><code>USER_SYSTEM_SUSPENDED</code>: job is user and system suspended</LI>
    * <LI><code>DONE</code>: job finished normally</LI>
    * <LI><code>FAILED</code>: job finished, but failed.</LI>
    * </UL>
    * <p>DRMAA always gets the status of jobId from DRM system.  No caching of
    * job state is done.</p>
    * <p>Jobs' user hold and user suspend states can be controled via control().
    * For affecting system hold and system suspend states the appropriate DRM
    * interfaces must be used.</p>
    * <p>The control method can be used to control job submitted outside of the scope
    * of the DRMAA session as long as the job identifier for the job is known.</p>
    * @return the program status
    * @param jobId the id of the job whose status is to be retrieved
    * @throws DrmaaException May be one of the following:
    * <UL>
    * <LI>DrmCommunicationException</LI>
    * <LI>AuthorizationException</LI>
    * <LI>InvalidJobException</LI>
    * </UL>
    */
   public int getJobProgramStatus (String jobId) throws DrmaaException {
      return this.nativeGetJobProgramStatus (jobId);
   }
   
   private native int nativeGetJobProgramStatus (String jobId) throws DrmaaException;
   
   /** Get a new job template.  The job template is used to set the
    * environment for submitted jobs.
    * @throws DrmaaException May be one of the following:
    * <UL>
    * <LI>DrmCommunicationException</LI>
    * </UL>
    * @return a blank JobTemplate object
    */
   public JobTemplate createJobTemplate () throws DrmaaException {
      int id = nativeAllocateJobTemplate ();
      
      return new JobTemplateImpl (this, id);
   }
   
   /** The deleteJobTemplate() method releases all resources associated with the DRMAA
    * JobTemplate.  Jobs that were submitted using the JobTemplate are not
    * affected.
    * @param jt the job template to delete
    * @throws DrmaaException May be one of the following:
    * <UL>
    * <LI>DRMAA_ERRNO_DRM_COMMUNICATION_FAILURE</LI>
    * </UL>
    */
   public void deleteJobTemplate (JobTemplate jt) throws DrmaaException {
      if (jt == null) {
         throw new NullPointerException ("JobTemplate is null");
      } else if (jt instanceof JobTemplateImpl) {
         nativeDeleteJobTemplate (((JobTemplateImpl)jt).getId ());
      } else {
         throw new InvalidJobTemplateException ();
      }
   }
   
   /** The getVersion() method returns a Version object containing
    * the major and minor version numbers of the DRMAA library. For a DRMAA
    * Java language binding specification 0.5 compliant implementation (e.g.
    * this binding) `0' and `5' will be the major and minor numbers,
    * respectively.
    * @return the version number as a Version object
    */
   public Version getVersion () {
      return new Version (0, 5);
   }
   
   /**
    * <p>The init() method initializes the Grid Engine DRMAA API library for
    * all threads of the process and creates a new DRMAA Session. This routine
    * must be called once before any other DRMAA call, except for
    * getDRMSystem(), getContact(), and getDRMAAImplementation().</p>
    * <p><i>contact</i> is an implementation dependent string which may be used
    * to specify which Grid Engine cell to use.  The contact string is composed
    * of a series of name=value pairs separated by semicolons.  The supported
    * name=value pairs are:</p>
    * <ul>
    *    <li>
    *      <code>session</code>: the id of the session to which to reconnect
    *    </li>
    * </ul>
    * <p>If <i>contact</i> is null or empty, the default Grid Engine cell will
    * be used.</p>
    * <p>Except for the above listed methods, no DRMAA methods may be called
    * before the init() function <b>completes</b>.  Any DRMAA method which is
    * called before the init() method completes will throw a
    * NoActiveSessionException.  Any additional call to init() by any thread
    * will throw a SessionAlreadyActiveException.</p>
    * <p>Once init() has been called, it is the responsibility of the developer to
    * ensure that the exit() will be called before the program terminates.</p>
    * @param contact implementation-dependent string that may be used to specify
    * which DRM system to use.  If null, will select the default DRM if there
    * is only one DRM implementation available.  Ignored in the current
    * implementation.
    * @throws DrmaaException Maybe be one of the following:
    * <UL>
    * <LI>InvalidContactStringException</LI>
    * <LI>AlreadyActiveSessionException</LI>
    * <LI>DefaultContactStringException</LI>
    * <LI>NoDefaultContactStringSelectedException</LI>
    * </UL>
    * @see #getContact()
    */
   public void init (String contact) throws DrmaaException {
      this.nativeInit (contact);
   }
   
   private native void nativeInit (String contact) throws DrmaaException;
   
   /** <p>The runBulkJobs() method submits a Grid Engine array job very much as if
    * the qsub option `-t <i>start</i>-<i>end</i>:<i>incr</i>' had been used
    * with the corresponding attributes defined in the DRMAA JobTemplate
    * <i>jt</i>.  The same constraints regarding qsub -t value ranges also apply to
    * the parameters <i>start</i>, <i>end</i>, and <i>incr</i>.  See the qsub(1) man
    * page for more information.</p>
    * <p> On success a String array containing job identifiers for each array job task is
    * returned.</p>
    * @return job identifier Strings identical to that returned by the
    * underlying DRM system
    * @param start the starting value for the loop index
    * @param end the terminating value for the loop index
    * @param incr the value by which to increment the loop index each iteration
    * @param jt the job template to be used to create the job
    * @throws DrmaaException May be one of the following:
    * <UL>
    * <LI>TryLaterException</LI>
    * <LI>DeniedByDrmException</LI>
    * <LI>DrmCommunicationException</LI>
    * <LI>AuthorizationException</LI>
    * </UL>
    */
   public List runBulkJobs (JobTemplate jt, int start, int end, int incr) throws DrmaaException {
      if (jt == null) {
         throw new NullPointerException ("JobTemplate is null");
      } else if (jt instanceof JobTemplateImpl) {
         String[] jobIds = this.nativeRunBulkJobs (((JobTemplateImpl)jt).getId (), start, end, incr);
         
         return Arrays.asList (jobIds);
      } else {
         throw new InvalidJobTemplateException ();
      }
   }
   
   private native String[] nativeRunBulkJobs (int jtId, int start, int end, int incr) throws DrmaaException;
   
   /** The runJob() method submits a Grid Engine job with attributes defined in
    * the DRMAA JobTemplate <i>jt</i>. On success, the job identifier is
    * returned.
    * @param jt the job template to be used to create the job
    * @throws DrmaaException May be one of the following:
    * <UL>
    * <LI>TryLaterException</LI>
    * <LI>DeniedByDrmException</LI>
    * <LI>DrmCommunicationException</LI>
    * <LI>AuthorizationException</LI>
    * </UL>
    * @return job identifier String identical to that returned by the
    * underlying DRM system
    */
   public String runJob (JobTemplate jt) throws DrmaaException {
      if (jt == null) {
         throw new NullPointerException ("JobTemplate is null");
      } else if (jt instanceof JobTemplateImpl) {
         return this.nativeRunJob (((JobTemplateImpl)jt).getId ());
      } else {
         throw new InvalidJobTemplateException ();
      }
   }
   
   private native String nativeRunJob (int jtId) throws DrmaaException;
   
   /** <p>The synchronize() method blocks the calling thread until all jobs
    * specified in <i>jobIds</i> have failed or finished execution. If
    * <i>jobIds</i> contains <code>JOB_IDS_SESSION_ALL</code>, then this method waits for
    * all jobs submitted during this DRMAA session.</p>
    * To prevent blocking indefinitely in this call, the caller may use
    * <i>timeout</i>, specifying how many seconds to wait for this call to
    * complete before timing out. The special value <code>TIMEOUT_WAIT_FOREVER</code>
    * can be
    * used to wait indefinitely for a result. The special value
    * <code>DRMAA_TIMEOUT_NO_WAIT</code> can be used to return immediately.  If the call
    * exits before <i>timeout</i>, all the specified jobs have completed or
    * the calling thread received an interrupt.  In both cases, the method will
    * throw an ExitTimeoutException.</p>
    * <p>The <i>dispose</i> parameter specifies how to treat reaping
    * information.  If <i>false</i> is passed to this paramter, job finish
    * information will still be available if wait() is called. If <i>true</i>
    * is passed, wait() will be unable to access this job's finish
    * information.</p>
    * @param jobIds the ids of the jobs to synchronize
    * @param timeout the maximum number of seconds to wait
    * @param dispose specifies how to treat reaping information
    * @throws DrmaaException May be one of the following:
    * <UL>
    * <LI>DrmCommunicationException</LI>
    * <LI>AuthorizationException</LI>
    * <LI>ExitTimeoutException</LI>
    * <LI>InvalidJobException</LI>
    * </UL>
    */
   public void synchronize (List jobIds, long timeout, boolean dispose) throws DrmaaException {
      this.nativeSynchronize ((String[])jobIds.toArray (new String[jobIds.size ()]), timeout, dispose);
   }
   
   private native void nativeSynchronize (String[] jobIds, long timeout, boolean dispose) throws DrmaaException;
   
   /** <p>The wait() function blocks the calling thread until a job fails or
    * finishes execution.  This routine is modeled on the UNIX wait4(3) routine.
    * If the special string <code>JOB_IDS_SESSION_ANY</code> is passed as <i>jobId</i>,
    * this routine will wait for any job from the session. Otherwise the
    * <i>jobId</i> must be the job identifier of a job or array job task that
    * was submitted during the session.</p>
    * <p>To prevent blocking indefinitely in this call, the caller may use
    * <i>timeout</i>, specifying how many seconds to wait for this call to
    * complete before timing out. The special value <code>TIMEOUT_WAIT_FOREVER</code>
    * can be uesd to wait indefinitely for a result. The special value
    * <code>TIMEOUT_NO_WAIT</code> can be used to return immediately.  If the call
    * exits before <i>timeout</i>, all the specified jobs have completed or the
    * calling thread received an interrupt.  In both cases, the method will
    * throw an ExitTimeoutException.</p>
    * <p>The routine reaps jobs on a successful call, so any subsequent calls to
    * wait() will fail, throwing an InvalidJobException, meaning that the job
    * has already been reaped.  This exception is the same as if the job were
    * unknown.  Returning due to an elapsed timeout or an interrupt does not
    * cause the job information to be reaped.  This means that, in that case it
    * is possible to issue wait() multiple times for the same <i>jobId</i>. </p>
    * <p>The wait() method will return a JobInfo object.  The JobInfo object
    * contains information about the job execution.  In particular, the JobInfo
    * object will contain the job id of the failed or finished job.  This is useful
    * when <code>JOB_IDS_SESSION_ANY</code> is passed as the <i>jobId</i>.</p>
    * @param jobId the id of the job for which to wait
    * @param timeout the maximum number of seconds to wait
    * @return the resource usage and status information
    * @throws DrmaaException May be one of the following:
    * <UL>
    * <LI>DrmCommunicationException</LI>
    * <LI>AuthorizationException</LI>
    * <LI>NoResourceUsageDataException</LI>
    * <LI>ExitTimeoutException</LI>
    * <LI>InvalidJobException</LI>
    * </UL>
    */
   public JobInfo wait (String jobId, long timeout) throws DrmaaException {
      JobInfoImpl jobInfo = this.nativeWait (jobId, timeout);
      
      return jobInfo;
   }
   
   private native JobInfoImpl nativeWait (String jobId, long timeout) throws DrmaaException;
   
   private native int nativeAllocateJobTemplate ();
   
   native void nativeSetAttributeValue (int jtId, String name, String value);
   
   native void nativeSetAttributeValues (int jtId, String name, String[] values);
   
   native String[] nativeGetAttributeNames (int jtId);
   
   native String[] nativeGetAttribute (int jtId, String name);
   
   native void nativeDeleteJobTemplate (int jtId);
   
   /** The getDRMAAImplementation() method returns a string containing the DRMAA
    * Java language binding implementation version information.  The
    * getDRMAAImplementation() method returns the same value before and after
    * init() is called.
    * @return DRMAA implementation information
    * @see #getVersion()
    */
   public String getDrmaaImplementation () {
      /* Because the DRMAA implementation is tightly bound to the DRM, there's
       * no need to distinguish between them.  Version information can be gotten
       * from getVersion() and language information is self-evident. */
      return this.getDrmSystem ();
   }
}
