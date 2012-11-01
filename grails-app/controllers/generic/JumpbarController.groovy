package generic

class JumpbarController {

    def jump = {
        [linkDest: params.linkDest, linkText: params.linkText,frameSource: params.frameSource, pageTitle: params.pageTitle]
    }
}
