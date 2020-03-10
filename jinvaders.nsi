; example2.nsi
;
; This script is based on example1.nsi, but it remember the directory, 
; has uninstall support and (optionally) installs start menu shortcuts.
;
; It will install example2.nsi into a directory that the user selects,

;--------------------------------

; The name of the installer
Name "JInvaders"

; The file to write
OutFile "setup.exe"

; The default installation directory
InstallDir $PROGRAMFILES\JInvaders

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\JInvaders" "Install_Dir"

; Request application privileges for Windows Vista
RequestExecutionLevel admin

;--------------------------------

; Pages

Page components
Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

;--------------------------------

; The stuff to install
Section "JInvaders"

  SectionIn RO
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  
  ; Put file there
  File "D:\temp\Jinvaders\jinvaders-2.2.exe"
  
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\NSIS_JInvaders "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\JInvaders" "DisplayName" "JInvaders"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\JInvaders" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\JInvaders" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\JInvaders" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\JInvaders"
  CreateShortCut "$SMPROGRAMS\JInvaders\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\JInvaders\JInvaders.lnk" "$INSTDIR\jinvaders-2.2.exe" "" "$INSTDIR\jinvaders.nsi" 0
  
SectionEnd

; Optional section (can be disabled by the user)
Section "Desktop Shortcut"

  CreateShortCut "$DESKTOP\JInvaders.lnk" "$INSTDIR\jinvaders-2.2.exe"
  
SectionEnd

;--------------------------------

; Uninstaller

Section "Uninstall"
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\JInvaders"
  DeleteRegKey HKLM SOFTWARE\NSIS_JInvaders

  Delete $DESKTOP\JInvaders.lnk
  
  ; Remove files and uninstaller
  
  Delete $INSTDIR\jinvaders-2.2.exe
  Delete $INSTDIR\uninstall.exe

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\JInvaders\*.*"

  ; Remove directories used
  RMDir "$SMPROGRAMS\JInvaders"
  RMDir "$INSTDIR"

SectionEnd
