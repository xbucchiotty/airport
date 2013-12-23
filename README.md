#Sequence diagrams
##Landing procedure

![Sequence as image](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=dGl0bGUgTGFuZGluZyBwcm9jZWR1cmUKCm9wdAAPCApQbGFuZS0-K0FpciBDb250cm9sOiBJbmNvbWluZwoACwstPgAWDVdhaXQgZnJlZSBydW53YXkAHQ4AUQU6AHcFKFIAGwUpCgoAZAgAEgtpbmcgd2l0aCByYW5kb20gZHVyYXRpb24AgQ4JADcGAEQGZWQocGxhbmUpCgBNBi0tPi0AgSoNABcOAIEvDS0AgQsHQ29udGFjdChncm91bmQpCmVuZACCCgYAgUoGb3IgYSBnYXRlIACCEAlHACUFAIIJEwALDi0-ABkQV2FpdGluZwBQBQCCJAVnYXRlAAQydGF4aXdheQBeEQCBSghQYXJrKFQAHwYsR2F0ZQCBSgtUYXhpIHRvAIFNBwCDZAgAgksJZWF2ZQCDFgkAgksJAIN-DAAVDgCEJAgAZwc6VGF4aQoAdActAIFvEgAaBQAXCT4ALggAgREFAINgGQBEC2F0AIFYBwCDcQgsZwCBWgUAWBoAgUwGAIIGBwAdDACCIQwAPw9lbmQ&s=modern-blue "Landing procedure")

    title Landing procedure

    opt Landing
    Plane->+Air Control: Incoming
    Air Control->Air Control: Wait free runway
    Air Control->Plane: Land(Runway)


    Plane->Plane: Landing with random duration
    Plane->+Runway: Landed(plane)
    Runway-->-Air Control: Landed(plane)
    Air Control->-Plane: Contact(ground)
    end

    opt Wait for a gate
    Plane->+Ground Control: Incoming
    Ground Control->Ground Control: Waiting for free gate
    Ground Control->Ground Control: Waiting for free taxiway
    Ground Control->-Plane: Park(Taxiway,Gate)
    end

    opt Taxi to a gate
    Plane->Runway: Leave(Runway)
    Runway-->Air Control:Leave(Runway)
    Plane->+Taxiway:Taxi
    Taxiway-->Ground Control: Taxi

    Taxiway->Taxiway: Taxiing with random duration
    Taxiway-->Ground Control: Leave(Taxiway)
    Taxiway-->Gate: Parked(plane,gate)
    Gate-->-Plane: Parked(plane,gate)
    end