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

#include "sge_all_listsL.h"
#include "sge_gdi.h"
#include "sig_handlers.h"
#include "parse_qsub.h"
#include "parse.h"
#include "usage.h"
#include "sge_prog.h"
#include "sgermon.h"
#include "sge_log.h"
#include "sge_feature.h"
#include "sge_language.h"
#include "sge_unistd.h"
#include "sge_answer.h"
#include "gdi_tsm.h"
#include "sge_id.h"
#include "sge_qinstance_state.h"

#include "msg_common.h"
#include "msg_clients_common.h"
#include "msg_qmod.h"

static lList *sge_parse_cmdline_qmod(char **argv, char **envp, lList **ppcmdline);
static lList *sge_parse_qmod(lList **ppcmdline, lList **ppreflist, u_long32 *pforce, u_long32 *paction);
static int qmod_usage(FILE *fp, char *what);

extern char **environ;

int main(int argc, char *argv[]);


int main(
int argc,
char **argv 
) {
   u_long32 action = 0, force = 0;
   lList *ref_list = NULL;
   lList *alp = NULL, *pcmdline = NULL;
   lListElem *aep;
   
   DENTER_MAIN(TOP_LAYER, "qmod");

   sge_gdi_param(SET_MEWHO, QMOD, NULL);
   if (sge_gdi_setup(prognames[QMOD], &alp)!=AE_OK) {
      answer_exit_if_not_recoverable(lFirst(alp));
      SGE_EXIT(1);
   }

   sge_setup_sig_handlers(QMOD);

   /*
   ** static func for parsing all qmod specific switches
   ** here we get action, force, ref_list
   */
   alp = sge_parse_cmdline_qmod(++argv, environ, &pcmdline);
   if(alp) {
      /*
      ** high level parsing error! show answer list
      */
      for_each(aep, alp) { 
         fprintf(stderr, "%s", lGetString(aep, AN_text));
      }
      lFreeList(alp);
      lFreeList(pcmdline);
      SGE_EXIT(1);
   }
   
   alp = sge_parse_qmod(&pcmdline, &ref_list, &force, &action);
   if(alp) {
      /*
      ** low level parsing error! show answer list
      */
      for_each(aep, alp) { 
         fprintf(stderr, "%s", lGetString(aep, AN_text));
      }
      lFreeList(alp);
      lFreeList(pcmdline);
      lFreeList(ref_list);
      SGE_EXIT(1);
   }

   if (ref_list) {
      lList *id_list = NULL;
      
      if (id_list_build_from_str_list(&id_list, &alp, 
                                      ref_list, action, force)) {
         alp = sge_gdi(SGE_CQUEUE_LIST, SGE_GDI_TRIGGER, &id_list, NULL, NULL);
      }
      id_list = lFreeList(id_list);
   }

   /*
   ** show answer list
   */
   for_each(aep, alp) { 
      fprintf(stdout, "%s", lGetString(aep, AN_text));
   }

   lFreeList(alp);
   lFreeList(ref_list);
   lFreeList(pcmdline); 
   SGE_EXIT(0);
   DEXIT;
   return 0;
}



/****
 **** sge_parse_cmdline_qmod (static)
 ****
 **** 'stage 1' parsing of qmod-options. Parses options
 **** with their arguments and stores them in ppcmdline.
 ****/ 
static lList *sge_parse_cmdline_qmod(
char **argv,
char **envp,
lList **ppcmdline 
) {
char **sp;
char **rp;
stringT str;
lList *alp = NULL;

   DENTER(TOP_LAYER, "sge_parse_cmdline_qmod");
   
   rp = argv;
   while(*(sp=rp)) {
      /* -help */
      if ((rp = parse_noopt(sp, "-help", "--help", ppcmdline, &alp)) != sp)
         continue;
        
      /* -f option */
      if ((rp = parse_noopt(sp, "-f", "--force", ppcmdline, &alp)) != sp)
         continue;
         
      /* -c option */
      if ((rp = parse_until_next_opt(sp, "-c", "--clear", ppcmdline, &alp)) != sp)
         continue;

      /* -s option */
      if ((rp = parse_until_next_opt(sp, "-s", "--suspend", ppcmdline, &alp)) != sp)
         continue;

      /* -us option */
      if ((rp = parse_until_next_opt(sp, "-us", "--unsuspend", ppcmdline, &alp)) != sp)
         continue;

      /* -d option */
      if ((rp = parse_until_next_opt(sp, "-d", "--disable", ppcmdline, &alp)) != sp)           
         continue;

      /* -r option */
      if ((rp = parse_until_next_opt(sp, "-r", "--reschedule", ppcmdline, &alp)) != sp)
         continue;

      /* -e option */
      if ((rp = parse_until_next_opt(sp, "-e", "--enable", ppcmdline, &alp)) != sp)
         continue;

#ifdef __SGE_QINSTANCE_STATE_DEBUG__
      if ((rp = parse_until_next_opt(sp, "-_e", "--_error", ppcmdline, &alp)) != sp)
         continue;
      if ((rp = parse_until_next_opt(sp, "-_o", "--_orphaned", ppcmdline, &alp)) != sp)
         continue;
      if ((rp = parse_until_next_opt(sp, "-_do", "--_dorphaned", ppcmdline, &alp)) != sp)
         continue;
      if ((rp = parse_until_next_opt(sp, "-_u", "--_unknown", ppcmdline, &alp)) != sp)
         continue;
      if ((rp = parse_until_next_opt(sp, "-_du", "--_dunknown", ppcmdline, &alp)) != sp)
         continue;
      if ((rp = parse_until_next_opt(sp, "-_c", "--_confambiguous", ppcmdline, &alp)) != sp)
         continue;
      if ((rp = parse_until_next_opt(sp, "-_dc", "--_dconfambiguous", ppcmdline, &alp)) != sp)
         continue;
#endif

      /* oops */
      sprintf(str, MSG_PARSE_INVALIDOPTIONARGUMENTX_S, *sp);
      qmod_usage(stderr, NULL);
      answer_list_add(&alp, str, STATUS_ESEMANTIC, ANSWER_QUALITY_ERROR);
      DEXIT;
      return alp;
   }
   DEXIT;
   return alp;  
}

/****
 **** sge_parse_qmod (static)
 ****
 **** 'stage 2' parsing of qmod-options. Gets the options from 
 **** ppcmdline, sets the force and action flags and puts the
 **** queue/job-names/numbers in ppreflist.
 ****/
static lList *
sge_parse_qmod(lList **ppcmdline, lList **ppreflist, 
               u_long32 *pforce, u_long32 *paction) 
{
stringT str;
lList *alp = NULL;
u_long32 helpflag;
int usageshowed = 0;

   DENTER(TOP_LAYER, "sge_parse_qmod");

   /* Loop over all options. Only valid options can be in the
      ppcmdline list. Except f_OPT all options are exclusive.
   */
   while(lGetNumberOfElem(*ppcmdline))
   {
      static const char *options[] = {
         "-c",
         "-d",
         "-r",
         "-e",
         "-s",
         "-us",
#ifdef __SGE_QINSTANCE_STATE_DEBUG__
         "-_e",
         "-_o",
         "-_do",
         "-_u",
         "-_du",
         "-_c",
         "-_dc",
#endif
         NULL
      };
      static const u_long32 transitions[] = {
         QI_DO_CLEARERROR,
         QI_DO_DISABLE,
         QI_DO_RESCHEDULE,
         QI_DO_ENABLE,
         QI_DO_SUSPEND,
         QI_DO_UNSUSPEND, 
#ifdef __SGE_QINSTANCE_STATE_DEBUG__
         QI_DO_SETERROR,
         QI_DO_SETORPHANED,
         QI_DO_CLEARORPHANED,
         QI_DO_SETUNKNOWN,
         QI_DO_CLEARUNKNOWN,
         QI_DO_SETAMBIGUOUS,
         QI_DO_CLEARAMBIGUOUS,
#endif
         QI_DO_NOTHING
      };
      int i;

      i = 0;
      *paction = QI_DO_NOTHING;
      while (options[i] != NULL) {
         if (parse_multi_stringlist(ppcmdline, options[i], &alp, 
                                    ppreflist, ST_Type, ST_name)) {
            *paction = transitions[i];
         }
         i++;
      }
   
      if (parse_flag(ppcmdline, "-help",  &alp, &helpflag)) {
         usageshowed = qmod_usage(stdout, NULL);
         break;
      }
      if (parse_flag(ppcmdline, "-f", &alp, pforce)) {
         continue;      
      }
      if (*paction != QI_DO_NOTHING) {
         break;
      }
   }
   if(lGetNumberOfElem(*ppcmdline)) {
     sprintf(str, MSG_PARSE_TOOMANYOPTIONS);
     if(!usageshowed)
        qmod_usage(stderr, NULL);
     answer_list_add(&alp, str, STATUS_ESEMANTIC, ANSWER_QUALITY_ERROR);
     DEXIT;
     return alp;
   }
   DEXIT;
   return alp;
}

/****
 **** qmod_usage (static)
 ****
 **** displays usage of qmod on file fp.
 **** Is what NULL, full usage will be displayed.
 ****
 **** Returns always 1.
 ****
 **** If what is a pointer to an option-string,
 **** only usage for that option will be displayed.
 ****   ** not implemented yet! **
 ****/
static int qmod_usage(
FILE *fp,
char *what 
) {
   dstring ds;
   char buffer[256];

   sge_dstring_init(&ds, buffer, sizeof(buffer));

   fprintf(fp, "%s\n", feature_get_product_name(FS_SHORT_VERSION, &ds));

   if(!what) {
      /* display full usage */
      fprintf(fp,"%s qmod [options]\n", MSG_SRC_USAGE); 
      fprintf(fp, "   [-c job_queue_list]  %s", MSG_QMOD_c_OPT_USAGE);
      fprintf(fp, "   [-d queue_list]      %s", MSG_QMOD_d_OPT_USAGE);
      fprintf(fp, "   [-e queue_list]      %s", MSG_QMOD_e_OPT_USAGE);
      fprintf(fp, "   [-f]                 %s", MSG_QMOD_f_OPT_USAGE);
      fprintf(fp, "   [-help]              %s", MSG_QMOD_help_OPT_USAGE);
      fprintf(fp, "   [-r job_queue_list]  %s", MSG_QMOD_r_OPT_USAGE);
      fprintf(fp, "   [-s job_queue_list]  %s", MSG_QMOD_s_OPT_USAGE);
      fprintf(fp, "   [-us job_queue_list] %s", MSG_QMOD_us_OPT_USAGE);
#ifdef __SGE_QINSTANCE_STATE_DEBUG__
      fprintf(fp, "   [-_e queue_list]     %s", MSG_QMOD_err_OPT_ISAGE);
      fprintf(fp, "   [-_o queue_list]     %s", MSG_QMOD_o_OPT_ISAGE);
      fprintf(fp, "   [-_do queue_list]    %s", MSG_QMOD_do_OPT_ISAGE);
      fprintf(fp, "   [-_u queue_list]     %s", MSG_QMOD_u_OPT_ISAGE);
      fprintf(fp, "   [-_du queue_list]    %s", MSG_QMOD_du_OPT_ISAGE);
      fprintf(fp, "   [-_c queue_list]     %s", MSG_QMOD_c_OPT_ISAGE);
      fprintf(fp, "   [-_dc queue_list]    %s", MSG_QMOD_dc_OPT_ISAGE);
#endif
      fprintf(fp, "job_queue_list          {job_tasks|queue}[{,| }{job_tasks|queue}{,| }...]\n");
      fprintf(fp, "queue_list              {queue}[{,| }{queue}{,| }...]\n");
      fprintf(fp, "job_tasks               job_id['.'task_id_range]\n");
      fprintf(fp, "task_id_range           task_id['-'task_id[':'step]]\n");

   } else {
      /* display option usage */
      fprintf(fp, MSG_QDEL_not_available_OPT_USAGE_S,what);
   }
   return 1;
}
