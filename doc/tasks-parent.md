

docker-compose -f thena-parent/doc/tasks-docker-db.yml up -d 

adminer ui
http://localhost:8091/

populate db with tasks
http://localhost:8080/q/tasks/api/demo/populate/1000

Drop order:

nested_10_refs
nested_10_tags
nested_10_commits
nested_10_treeitems
nested_10_trees
nested_10_blobs
repos