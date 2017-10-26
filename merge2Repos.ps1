# Assume the current directory is where we want the new repository to be created
# Create the new repository
git init

# Before we do a merge, we have to have an initial commit, so we'll make a dummy commit
dir > deleteme.txt
git add .
git commit -m "Initial merger commit"

# Add a remote for and fetch the old repo
git remote add -f Front-End https://saqibahmed515@bitbucket.org/expertflow-ondemand/umm_frontend.git

# Merge the files from Front-End/master into new/master
git merge Front-End/master --allow-unrelated-histories

# Clean up our dummy file because we don't need it any more
git rm .\deleteme.txt
git commit -m "Clean up initial file"

# Move the Front-End repo files and folders into a subdirectory so they don't collide with the other repo coming later
mkdir Front-End
dir -exclude Front-End | %{git mv $_.Name Front-End}

# Commit the move
git commit -m "Move Front-End files into subdir"

# Do the same thing for Back-End
git remote add -f Back-End https://saqibahmed515@bitbucket.org/expertflow-ondemand/umm.git
git merge Back-End/master --allow-unrelated-histories
mkdir Back-End
dir -exclude Front-End,Back-End | %{git mv $_.Name Back-End}
git commit -m "Move Back-End files into subdir"
