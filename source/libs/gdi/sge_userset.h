#ifndef _SGE_USERSET_H
#define _SGE_USERSET_H
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

#include "sge_usersetL.h"
#include "sge_mirror.h"

extern lList *Master_Userset_List;

int is_deadline_user(char *username, lList *lp);

lListElem *userset_list_locate(lList *lp, const char *name);

int userset_update_master_list(sge_event_type type, sge_event_action action,
                               lListElem *event, void *clientdata);

int sge_verify_userset_entries(lList *u, lList **alpp, int start_up);

int verify_acl_list(lList **alpp, lList *acl_list, const char *attr_name, 
                    const char *obj_descr, const char *obj_name);


#endif /* _SGE_USERSET_H */



