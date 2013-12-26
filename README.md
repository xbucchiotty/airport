#Sequence diagrams
##Short Landing procedure
![Sequence as image](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=dGl0bGUgU2hvcnQgTGFuZGluZyBwcm9jZWR1cmUKCiAgICBvcAASCQALBVBsYW5lLS0-K0FpciBDb250cm9sOiBJbmNvbQAbCAAPCy0-ABoNV2FpdCBmcmVlIHJ1bndheQAdEgBaBToAgQgFKFIAHwUpCgCBAwYAcQgAFwtpbmcgd2l0aCByYW5kb20gZHVyYXRpb24AgRwNAIEbDUxhbmRlZChwbGFuZSkAgRsSLQB4B0NvbnRhY3QoZ3JvdW5kACMGZW5kAIIICgCBQwZvciBhIGdhdGUAggwORwAxBQCCBhcADw4tPgAdEFdhaXRpbmcAWAUAgiUFAFcJAAkxdGF4aQCCXQgAZxAAgWIIVGF4aUFuZFBhcmsoVAAqBixHYXQAghYHAIFnDVRheGkgdG8AgV8UAIQADEhhc0xlZnQAgzUJAIQpDACCChBIYXNFbnRlcmVkAHUIKQCDWBVUYXhpAINIJQBOEwCBCAUAggQHAHAeUGFya2VkKACBaBdVbmxvYQCGHAZhc3NlbmdlcnMAhQUUAIM1DHUAFCAAgRgYAIJ5CgCEdAg&s=modern-blue "Landing procedure")

    title Short Landing procedure

    opt Landing
    Plane-->+Air Control: Incoming
    Air Control->Air Control: Wait free runway
    Air Control->Plane: Land(Runway)


    Plane-->Plane: Landing with random duration
    Plane-->Air Control: Landed(plane)
    Air Control->-Plane: Contact(ground)
    end

    opt Wait for a gate
    Plane-->+Ground Control: Incoming
    Ground Control->Ground Control: Waiting for free gate
    Ground Control->Ground Control: Waiting for free taxiway
    Ground Control->-Plane: TaxiAndPark(Taxiway,Gate)
    end

    opt Taxi to a gate
    Plane-->Air Control:HasLeft(Runway)
    Plane-->Ground Control: HasEntered(Taxiway)

    Plane-->Plane: Taxiing with random duration
    Plane-->Ground Control: HasLeft(taxiway)
    Plane-->Ground Control: Parked(Gate)
    end

    opt Unloading passengers
    Plane-->Plane: Waiting for unloading passengers
    Plane-->Ground Control: HasLeft(Gate)

    end

##Long Procedure
![Sequence as image](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=dGl0bGUgTGFuZGluZyBwcm9jZWR1cmUKCiAgICBvcHQAEwgACwVQbGFuZS0tPitBaXIgQ29udHJvbDogSW5jb20AGwgADwstPgAaDVdhaXQgZnJlZSBydW53YXkAHRIAWgU6AIEIBShSAB8FKQoAgQMGAHEIABcLaW5nIHdpdGggcmFuZG9tIGR1cmF0aW9uAIEcDS0AgRwNTGFuZGVkAIE9DQBhBgAODQCBNQ0tAIENB0NvbnRhY3QoZ3JvdW5kKQCCGQVlbmQAgh0KAIFYBm9yIGEgZ2F0ZQCCIQ5HADEFAIIbFwAPDi0-AB0QV2FpdGluZwBYBQCCOgUAVwkACTF0YXhpAIJyCABnEACBYghUYXhpQW5kUGFyayhUACoGLEdhdGUAgWETVGF4aSB0bwCBXxQAglAISGFzTGVmdACEMQ0AhDEMAAwUAG8HOlRheGlpbmdUb0dhdGUAeRMAhQYIAIJOEEhhc0VudGVyZWQAgTkIKQCFSQYAgUgHAFgLAIFpBQCELR0AKgpHYXRlOiBQYXJrAIQ9BwBFCgCFMgcAFgYoAIESEACBDRQAggYPAINkEQA6EQCEYA1VbmxvYQCHKwZhc3NlbmdlcnMAhhQUAIQvDHUAFCAAgVoGAHkjAINAFACENQhlcm1pbmF0AIg5B2VuZA&s=modern-blue "Landing procedure")

    title Landing procedure

    opt Landing
    Plane-->+Air Control: Incoming
    Air Control->Air Control: Wait free runway
    Air Control->Plane: Land(Runway)


    Plane-->Plane: Landing with random duration
    Plane-->-Air Control: Landed
    Plane-->Runway: Landed
    Air Control->-Plane: Contact(ground)
    end

    opt Wait for a gate
    Plane-->+Ground Control: Incoming
    Ground Control->Ground Control: Waiting for free gate
    Ground Control->Ground Control: Waiting for free taxiway
    Ground Control->-Plane: TaxiAndPark(Taxiway,Gate)
    end

    opt Taxi to a gate
    Plane-->Runway: HasLeft
    Plane-->Air Control:HasLeft
    Plane-->Taxiway:TaxiingToGate(Taxiway,Gate)
    Plane-->Ground Control: HasEntered(Taxiway)

    Taxiway-->Taxiway: Taxiing with random duration
    Taxiway-->Gate: Parked
    Taxiway-->Plane: Parked
    Plane->Ground Control: Parked
    end

    opt Unloading passengers
    Plane-->Plane: Waiting for unloading passengers
    Plane-->Gate: HasLeft
    Plane->Ground Control: HasLeft
    Plane-->Plane: Terminate

    end