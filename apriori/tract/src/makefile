#-----------------------------------------------------------------------
# File    : makefile
# Contents: build item and transaction management (on Unix systems)
# Author  : Christian Borgelt
# History : 2008.10.05 file created from apriori makefile
#           2009.10.09 module pfxtree added
#           2010.02.10 module pfxtree replaced by module clomax
#           2010.10.08 changed standard from -ansi to -std=c99
#           2011.05.06 changed to double support reporting/recording
#-----------------------------------------------------------------------
SHELL   = /bin/bash
THISDIR = ../../tract/src
UTILDIR = ../../util/src

CC      = gcc -std=c99
# CC      = g++
CFBASE  = -Wall -pedantic $(ADDFLAGS)
CFLAGS  = $(CFBASE) -DNDEBUG -O3
# CFLAGS  = $(CFBASE) -DNDEBUG -O3 -DALIGN8
# CFLAGS  = $(CFBASE) -g
# CFLAGS  = $(CFBASE) -g -DALIGN8
INCS    = -I$(UTILDIR)

#-----------------------------------------------------------------------
# Build Module
#-----------------------------------------------------------------------
all:       tract.o report.o repcm.o clomax.o

#-----------------------------------------------------------------------
# Item and Transaction Management
#-----------------------------------------------------------------------
tract.o:   tract.h $(UTILDIR)/arrays.h $(UTILDIR)/symtab.h
tract.o:   tract.c makefile
	$(CC) $(CFLAGS) $(INCS) -c tract.c -o $@

tatree.o:  tract.h $(UTILDIR)/arrays.h $(UTILDIR)/symtab.h
tatree.o:  tract.c makefile
	$(CC) $(CFLAGS) $(INCS) -DTATREEFN -c tract.c -o $@

#-----------------------------------------------------------------------
# Item Set Reporter Management
#-----------------------------------------------------------------------
report.o:  report.h tract.h $(UTILDIR)/arrays.h $(UTILDIR)/symtab.h
report.o:  report.c makefile
	$(CC) $(CFLAGS) $(INCS) -c report.c -o $@

repdbl.o:  report.h tract.h $(UTILDIR)/arrays.h $(UTILDIR)/symtab.h
repdbl.o:  report.c makefile
	$(CC) $(CFLAGS) $(INCS) -DSUPP_T=double -c report.c -o $@

repcm.o:   report.h tract.h $(UTILDIR)/arrays.h $(UTILDIR)/symtab.h
repcm.o:   report.c makefile
	$(CC) $(CFLAGS) $(INCS) -DISR_CLOMAX -c report.c -o $@

repcmd.o:  report.h tract.h $(UTILDIR)/arrays.h $(UTILDIR)/symtab.h
repcmd.o:  report.c makefile
	$(CC) $(CFLAGS) $(INCS) -DSUPP_T=double -DISR_CLOMAX -c report.c -o $@

#-----------------------------------------------------------------------
# Closed and Maximal Frequent Item Set Tree Management
#-----------------------------------------------------------------------
clomax.o:  clomax.h $(UTILDIR)/arrays.h $(UTILDIR)/memsys.h
clomax.o:  clomax.c makefile
	$(CC) $(CFLAGS) $(INCS) -c clomax.c -o $@

cmdbl.o:   clomax.h $(UTILDIR)/arrays.h $(UTILDIR)/memsys.h
cmdbl.o:   clomax.c makefile
	$(CC) $(CFLAGS) $(INCS) -DSUPP_T=double -c clomax.c -o $@

#-----------------------------------------------------------------------
# Clean up
#-----------------------------------------------------------------------
clean:
	rm -f *.o *~ *.flc core
