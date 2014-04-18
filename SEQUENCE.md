#Sequence diagrams
##Short Landing procedure
![Sequence as image](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=ICAgIHRpdGxlIFNob3J0IExhbmRpbmcgcHJvY2VkdXJlCgogICAgb3AAEgkACwVQbGFuZS0tPitBaXIgQ29udHJvbDogSW5jb20AGwgADwstPgAaDVdhaXQgZnJlZSBydW53YXkAHRIAWgU6AIEIBShSAB8FKQBsCwBADkFjawCBHgYAgQwIADILaW5nIHdpdGggcmFuZG9tIGR1cmF0aW9uAIE3DQCBNg1IYXNMYW5kZWQocGxhbmUAcQYAgT4NLQCBFgdDb250YWN0KGdyb3VuZACBAh4gICAgZW5k&s=modern-blue)

    title Short Landing procedure

    opt Landing
    Plane-->+Air Control: Incoming
    Air Control->Air Control: Wait free runway
    Air Control->Plane: Land(Runway)
    Plane->Air Control: Ack

    Plane-->Plane: Landing with random duration
    Plane-->Air Control: HasLanded(plane)
    Air Control->-Plane: Contact(ground)
    Plane->Air Control: Ack
    end

