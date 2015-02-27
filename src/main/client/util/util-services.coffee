angular
  .module 'tracker.util'


  ## A small utility service that is handy for looking up element hierarchies. Could be done
  ## in jQuery, I guess. This is in case we drop it, some day, as we might.
  .factory 'findParentCell', () ->

    findCell = (element, className) ->
      if element.hasClass className
        return element
      parent = element.parent()
      return parent if parent[0] == element[0]
      return element if parent.length == 0
      return findCell parent, className

    findCell

  