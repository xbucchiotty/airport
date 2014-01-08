#Sequence diagrams
##Short Landing procedure
![Sequence as image](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=dGl0bGUgU2hvcnQgTGFuZGluZyBwcm9jZWR1cmUKCiAgICBvcAASCQALBVBsYW5lLS0-K0FpciBDb250cm9sOiBJbmNvbQAbCAAPCy0-ABoNV2FpdCBmcmVlIHJ1bndheQAdEgBaBToAgQgFKFIAHwUpCgCBAwYAcQgAFwtpbmcgd2l0aCByYW5kb20gZHVyYXRpb24AgRwNAIEbDUxhbmRlZChwbGFuZSkAgRsSLQB4B0NvbnRhY3QoZ3JvdW5kACMGZW5kAIIICgCBQwZvciBhIGdhdGUAggwORwAxBQCCBhcADw4tPgAdEFdhaXRpbmcAWAUAgiUFAFcJAAkxdGF4aQCCXQgAZxAAgWIIVGF4aUFuZFBhcmsoVAAqBixHYXQAghYHAIFnDVRheGkgdG8AgV8UAIQADEhhc0xlZnQAgzUJAIQpDACCChBIYXNFbnRlcmVkAHUIKQCDWBVUYXhpAINIJQBOEwCBCAUAggQHAHAeUGFya2VkKACBaBdVbmxvYQCGHAZhc3NlbmdlcnMAhQUUAIM1DHUAFCAAgRgYAIJ5CgCEdAg&s=modern-blue "Landing procedure")

    title Short Landing procedure

    opt Landing
    Plane-->+Air Control: Incoming
    Air Control->Air Control: Wait free runway
    Air Control->Plane: Land(Runway)
    Plane->Air Control: Ack

    Plane-->Plane: Landing with random duration
    Plane-->Air Control: Landed(plane)
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