#-----------------------------------------------------------------------
# File    : tract.mak
# Contents: build item and transaction management (on Windows systems)
# Author  : Christian Borgelt
# History : 2008.10.05 file created from apriori makefile
#           2011.05.06 changed to double support reporting/recording
#-----------------------------------------------------------------------
THISDIR  = ..\..\tract\src
UTILDIR  = ..\..\util\src

CC       = cl.exe
DEFS     = /D WIN32 /D NDEBUG /D _CONSOLE /D _CRT_SECURE_NO_WARNINGS
CFLAGS   = /nologo /W3 /O2 $(DEFS) /c
INCS     = /I $(UTILDIR)

LD       = link.exe
LDFLAGS  = /nologo /subsystem:console /incremental:no /machine:X86
LIBS     = 

#-----------------------------------------------------------------------
# Build Module
#-----------------------------------------------------------------------
all:       tract.obj report.obj clomax.obj

#-----------------------------------------------------------------------
# Item and Transaction Management
#-----------------------------------------------------------------------
tract.obj:  tract.h $(UTILDIR)\symtab.h
tract.obj:  tract.c tract.mak
	$(CC) $(CFLAGS) $(INCS) tract.c /Fo$@

tatree.obj: tract.h $(UTILDIR)\symtab.h
tatree.obj: tract.c tract.mak
	$(CC) $(CFLAGS) $(INCS) /D TATREEFN tract.c /Fo$@

#-----------------------------------------------------------------------
# Item Set Reporter Management
#-----------------------------------------------------------------------
report.obj: report.h tract.h $(UTILDIR)\symtab.h
report.obj: report.c tract.mak
	$(CC) $(CFLAGS) $(INCS) report.c /Fo$@

repdbl.obj: report.h tract.h $(UTILDIR)\symtab.h
repdbl.obj: report.c tract.mak
	$(CC) $(CFLAGS) $(INCS) /D SUPP_T=double report.c /Fo$@

repcm.obj:  report.h tract.h $(UTILDIR)/symtab.h
repcm.obj:  report.c tract.mak
	$(CC) $(CFLAGS) $(INCS) /D ISR_CLOMAX report.c /Fo$@

repcmd.obj: report.h tract.h $(UTILDIR)/symtab.h
repcmd.obj: report.c tract.mak
	$(CC) $(CFLAGS) $(INCS) /D SUPP_T=double /D ISR_CLOMAX report.c /Fo$@

#-----------------------------------------------------------------------
# Prefix Tree Management for Closed/Maximal Filtering
#-----------------------------------------------------------------------
clomax.obj: clomax.h $(UTILDIR)\arrays.h $(UTILDIR)\memsys.h
clomax.obj: clomax.c tract.mak
	$(CC) $(CFLAGS) $(INCS) -c clomax.c /Fo$@

cmdbl.obj:  clomax.h $(UTILDIR)\arrays.h $(UTILDIR)\memsys.h
cmdbl.obj:  clomax.c tract.mak
	$(CC) $(CFLAGS) $(INCS) /D SUPP_T=double -c clomax.c /Fo$@

#-----------------------------------------------------------------------
# Clean up
#-----------------------------------------------------------------------
clean:
	-@erase /Q *~ *.obj *.idb *.pch $(PRGS)
