#Sequence diagrams
##Short Landing procedure
![Sequence as image](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=dGl0bGUgU2hvcnQgTGFuZGluZyBwcm9jZWR1cmUKCiAgICBvcAASCQALBVBsYW5lLS0-K0FpciBDb250cm9sOiBJbmNvbQAbCAAPCy0-ABoNV2FpdCBmcmVlIHJ1bndheQAdEgBaBToAgQgFKFIAHwUpAGwLAEAOQWNrAIEeBgCBDAgAMgtpbmcgd2l0aCByYW5kb20gZHVyYXRpb24AgTcNAIE2DUhhc0xhbmRlZChwbGFuZQBxBgCBPg0tAIEWB0NvbnRhY3QoZ3JvdW5kAIECHiAgICBlbmQAgkIKAIF9Bm9yIGEgZ2F0ZQCCRg5HAE0FAIJAFwAPDi0-AB0QV2FpdGluZwBYBQCCXwV0YXhpAIJdCAAtEACBRAhUYXhpKFQAIwYAgl0NAHsQAIE4FVRheGkAgjkZSGFzTGVmdACEKQ0AgVEQVGF4aWluZwCDMhUAFQcAgyUiAIImEEVuZE9mAIESCQCCBjEAgwYRAIMGEFBhcmtBdChHYXQAhB8HAIIHLFVubG9hAIZjBmFzc2VuZ2VycwCFVgwAg1gPAIUTBVBhcmtlZACFVxQAg2gMdQA8HgCEHxIAgxkIAIUdCA&s=modern-blue "Landing procedure")

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

    opt Wait for a gate
    Plane-->+Ground Control: Incoming
    Ground Control->Ground Control: Waiting for free taxiway
    Ground Control->-Plane: Taxi(Taxiway)
    Plane->Ground Control: Ack
    end

    opt Taxi
    Plane-->Air Control:HasLeft
    Plane-->Ground Control: Taxiing

    Plane-->Plane: Taxiing with random duration
    Plane-->Ground Control: EndOfTaxi
    Ground Control->Ground Control: Waiting for free gate
    Plane-->Ground Control: ParkAt(Gate)
    Plane->Ground Control: Ack
    end

    opt Unloading passengers
    Plane-> Ground Control: HasParked
    Plane-->Plane: Waiting for unloading passengers
    Plane-->Ground Control: HasLeft

    end