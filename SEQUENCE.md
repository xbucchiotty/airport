#Sequence diagrams
##Short Landing procedure
![Sequence as image](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=dGl0bGUgU2hvcnQgTGFuZGluZyBwcm9jZWR1cmUKCiAgICBvcAASCQALBVBsYW5lLS0-K0FpciBDb250cm9sOiBJbmNvbQAbCAAPCy0-ABoNV2FpdCBmcmVlIHJ1bndheQAdEgBaBToAgQgFKFIAHwUpAGwLAEAOQWNrAIEeBgCBDAgAMgtpbmcgd2l0aCByYW5kb20gZHVyYXRpb24AgTcNAIE2DUhhc0xhbmRlZChwbGFuZQBxBgCBPg0tAIEWB0NvbnRhY3QoZ3JvdW5kAIECHgCBDBNXYWl0aW5nIGZvciB0YXhpd2F5cwCBWAwtAIEGEWVmdACDBwVlbmQ&s=modern-blue)

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
    Plane-->Plane: Waiting for taxiways
    Plane->-Air Control: HasLeft
    end

