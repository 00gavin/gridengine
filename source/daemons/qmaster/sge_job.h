#ifndef __SGE_JOB_H
#define __SGE_JOB_H
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

#ifndef __SGE_GDI__INTERN_H
#   include "sge_gdi_intern.h"
#endif

#include "sge_eventL.h"

int sge_gdi_add_job(lListElem *jep, lList **alpp, lList **lpp, char *ruser, char *rhost, sge_gdi_request *request);
int sge_gdi_copy_job(lListElem *jep, lList **alpp, lList **lpp, char *ruser, char *rhost, sge_gdi_request *request);

int sge_gdi_mod_job(lListElem *jep, lList **alpp, char *ruser, char *rhost, int sub_command);

int sge_gdi_del_job(lListElem *jep, lList **alpp, char *ruser, char *rhost, int sub_command);

void sge_add_job_event(ev_event type, lListElem *jep, lListElem *jatep);

void sge_add_jatask_event(ev_event type, lListElem *jep, lListElem *jatask);

void job_suc_pre(lListElem *jep);

lListElem *sge_locate_job(u_long32);

/* searches by id or jobname */
lListElem *locate_job_by_identifier(const char *s, const char *owner);

void get_rid_of_job(lList **alpp, lListElem *jep, lListElem *jatep, int force, 
                    sge_pack_buffer *pb, char *pb_host, char *ruser, 
                    char *rhost, const char *err_str, char *commproc);

#endif /* __SGE_JOB_H */
