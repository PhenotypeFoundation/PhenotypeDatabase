This directory contains local versions of plugins. This makes changes in 
the plugins much easier, without the need to publish plugins.

Steps taken:
git remote add -f plugin-sam https://github.com/PhenotypeFoundation/SAM.git
git remote add -f plugin-gdt https://github.com/PhenotypeFoundation/GDT.git
git remote add -f plugin-gdtimporter https://github.com/PhenotypeFoundation/GDTImporter.git
git remote add -f plugin-dbxpbase https://github.com/PhenotypeFoundation/dbxpBase.git

git subtree add --prefix=local-plugins/SAM --squash plugin-sam/plugin
git subtree add --prefix=local-plugins/GDT --squash plugin-gdt/master
git subtree add --prefix=local-plugins/GDTImporter --squash plugin-gdtimporter/master
git subtree add --prefix=local-plugins/dbxpBase --squash plugin-dbxpbase/master

See also:
http://blogs.atlassian.com/2013/05/alternatives-to-git-submodule-git-subtree/
https://hpc.uni.lu/blog/2014/understanding-git-subtree/

