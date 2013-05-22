## lprimporter 4.0
*  Flyttet fra https://svn.softwareborsen.dk/Behandlingsrelation/BehandlingsRelation/trunk/brs-registerimportjob/

## lprimporter 4.1
*  Opgradér afhængighed til sdm-parent, så det vagrant-testmiljø parent stiller til rådighed, er forberedt for
   lprimporter-modulet
   
## lprimporter 4.2   
*  Opgrading til sdm4-core 4.3, der løser
   NSPSUPPORT-126: ParserExecutor logger filers absolutte stier og md5-summer inden parser behandler dem
*  SDM-28: Importeren ignorerer nu records med SKS Numre.

## lprimporter 4.3
*  SDM-28: Rettelse fra 4.2 er rettet tilbage og lavet om så vi nu accepterer alle numre som overholder længde kravene
           hvis ikke så ignore vi recorden og fortsætter.

## lprimporter 4.4
*  SDM-33: Rettet så vi nu godtager ydernumre på under 5 tegn.
*  Opdateret core, parent og testutils dependencies til nyeste releases